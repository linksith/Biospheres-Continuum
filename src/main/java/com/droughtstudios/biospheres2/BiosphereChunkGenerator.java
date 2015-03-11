package com.droughtstudios.biospheres2;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereChunkGenerator extends ChunkProviderGenerate {

	private final MapGenCaves mCaveGen;
//	private final BiosphereMapGenStructures.BiosphereStronghold mStrongholdGen;
	private final MapGenScatteredFeature mScatteredFeatureGen;
	private final BiosphereMapGenStructures.BiosphereOceanMonument mOceanMonumentGen;
	private final BiosphereMapGenStructures.BiosphereVillage mVillageGen;

	private BiomeGenBase[] mBiomeGens;

	private Random mRandom;

	private World mWorld;

	public BiosphereChunkGenerator(World worldIn, long seed, boolean p_i45636_4_, String p_i45636_5_) {
		super(worldIn, seed, p_i45636_4_, p_i45636_5_);
		mWorld = worldIn;
		mCaveGen = new MapGenCaves();
//		mStrongholdGen = new BiosphereMapGenStructures.BiosphereStronghold();
		mScatteredFeatureGen = new MapGenScatteredFeature();
		mOceanMonumentGen = new BiosphereMapGenStructures.BiosphereOceanMonument();
		mVillageGen = new BiosphereMapGenStructures.BiosphereVillage();
		mRandom = new Random(seed);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public Chunk provideChunk(int areaX, int areaY) {
		mRandom.setSeed((long)areaX * 341873128712L + (long)areaY * 132897987541L);
		ChunkPrimer chunkPrimer = new ChunkPrimer();

		BiosphereInfo biosphere = BiosphereChunkManager.get(mWorld).getBiosphereAtArea(areaX, areaY);
		double biomeRadiusSq = biosphere.radius * biosphere.radius;
        double OTRadiusSq = biosphere.oreThingRadius * biosphere.oreThingRadius;

		// load default blocks
		this.func_180518_a(areaX, areaY, chunkPrimer);

		// load biome data for generating
		mBiomeGens = mWorld.getWorldChunkManager().loadBlockGeneratorData(mBiomeGens, areaX * 16, areaY * 16, 16, 16);

		// generate terrain
		this.func_180517_a(areaX, areaY, chunkPrimer, mBiomeGens);

		// generate caves
		mCaveGen.func_175792_a(this, mWorld, areaX, areaY, chunkPrimer);

		// todo ravines?

		generateFeatures(areaX, areaY, chunkPrimer);

		// cut out sphere
		for (int x = 0;x < 16;x++) {
			for (int z = 0;z < 16;z++) {

				double dx = (areaX * 16 + x) - biosphere.worldCenter.xCoord;
				double dz = (areaY * 16 + z) - biosphere.worldCenter.zCoord;
				double distance2dSq = (dx * dx) + (dz * dz);

                double dotx = (areaX * 16 + x) - biosphere.oreThingCenter.xCoord;
                double dotz = (areaY * 16 + z) - biosphere.oreThingCenter.zCoord;
                double distanceot2dSq = (dotx * dotx) + (dotz * dotz);

				// point not in biosphere, ensure all is air
				// terrain generation above may have added additional terrain outside of circle
				if (distance2dSq > biomeRadiusSq && distanceot2dSq > OTRadiusSq) {
					for (int y = 0;y < 256;y++) {
						chunkPrimer.setBlockState(x, y, z, Blocks.air.getDefaultState());
					}
					continue;
				}

				// cut out sphere itself when within biome's circle radius
				boolean underGround = false;
				for (int y = 255; y >= 0; y--) {
					double dy = y - biosphere.worldCenter.yCoord;
                    double doty = y - biosphere.oreThingCenter.yCoord;

					double distance3dSq = distance2dSq + (dy * dy);
                    double distanceot3dSq = distanceot2dSq + (doty * doty);

					// get current block state
					Block block = chunkPrimer.getBlockState(x, y, z).getBlock();
					boolean isBlockSolid = block.getMaterial().blocksMovement() || block instanceof BlockFalling;

					// solid blocks found in this column, so we are now 'underground'
					underGround |= isBlockSolid;

                    //ore buddy
                    boolean inOreBuddy = false;
                    if ( distanceot3dSq <= OTRadiusSq) {
                        inOreBuddy = true;
                    }

					// cut sphere
					if (distance3dSq > biomeRadiusSq && distanceot3dSq > OTRadiusSq) {
						chunkPrimer.setBlockState(x, y, z, Blocks.air.getDefaultState());
					}

					// ensure sphere is closed - biosphere.radius equates to a difference of 1 in the squared distances
					else if (BiosphereInfo.DOME_ENABLED && !isBlockSolid &&
					         biomeRadiusSq - distance3dSq <= biosphere.radius * 2) {

						// close sphere below ground with stone
						// todo consider the end and the nether
						if (underGround || inOreBuddy) {
							chunkPrimer.setBlockState(x, y, z, Blocks.stone.getDefaultState());
						}

						// close sphere above ground with glass
						else if (!inOreBuddy){
							chunkPrimer.setBlockState(x, y, z, BlockDome.blockDome.getDefaultState());
						}
					}
				}
			}
		}

		Chunk chunk = new Chunk(mWorld, chunkPrimer, areaX, areaY);

		// todo wtf is with this byte array?
		byte[] abyte = chunk.getBiomeArray();

		for (int k = 0; k < abyte.length; ++k)
		{
			abyte[k] = (byte)mBiomeGens[k].biomeID;
		}

		chunk.generateSkylightMap();
		return chunk;
	}

	@Override
	public void func_180514_a(Chunk p_180514_1_, int areaX, int areaY) {
		generateFeatures(areaX, areaY, null);
	}

	private void generateFeatures(int areaX, int areaY, ChunkPrimer chunkPrimer) {
		BiosphereInfo biosphere = BiosphereChunkManager.get(mWorld).getBiosphereAtArea(areaX,
		                                                                               areaY);

		boolean inRootRadius = false;

		// no need to recalculate if we already have; ie, did we come from provideChunk()?
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				// inside inner radius
				if (biosphere.inInnerRadius(areaX * 16 + x, areaY * 16 + y)) {
					inRootRadius = true;
				}
			}
		}

		if (inRootRadius) {

			// todo add stronghold support
//			mStrongholdGen.func_175792_a(this, mWorld, areaX, areaX, chunkPrimer);

			// todo mineshafts?

			mVillageGen.func_175792_a(this, mWorld, areaX, areaY, chunkPrimer);

			// todo scattered features

			mOceanMonumentGen.func_175792_a(this, mWorld, areaX, areaY, chunkPrimer);
		}
	}

	// Ocean monument generation =======================================================================================

	@Override
	public boolean func_177460_a(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_) {
		return mOceanMonumentGen.func_175794_a(mWorld, mRandom, new ChunkCoordIntPair(p_177460_3_, p_177460_4_));
	}

	@Override
	public List func_177458_a(EnumCreatureType p_177458_1_, BlockPos p_177458_2_) {
        if (p_177458_1_ == EnumCreatureType.MONSTER && mOceanMonumentGen.func_175796_a(mWorld, p_177458_2_)) {
			return mOceanMonumentGen.func_175799_b();
		}
		return super.func_177458_a(p_177458_1_, p_177458_2_);
	}

	// Stronghold generation ===========================================================================================

//	@Override
//	public BlockPos func_180513_a(World worldIn, String p_180513_2_, BlockPos p_180513_3_) {
//		return "Stronghold".equals(p_180513_2_) && mStrongholdGen != null ? mStrongholdGen.func_180706_b(worldIn, p_180513_3_) : null;
//	}

	// Populate chunk with structure gen ===============================================================================

	@SubscribeEvent
	public void onPopulateChunk(PopulateChunkEvent.Pre event) {
		ChunkCoordIntPair coord = new ChunkCoordIntPair(event.chunkX, event.chunkZ);

		mOceanMonumentGen.func_175794_a(event.world, event.rand, coord);
		mVillageGen.func_175794_a(mWorld, mRandom, coord);
//		mStrongholdGen.func_175794_a(mWorld, mRandom, coord);
	}
}
