package com.droughtstudios.biospheres2;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereChunkGenerator extends ChunkProviderGenerate {

	private final MapGenCaves mCaveGen;
	private final MapGenStronghold mStrongholdGen;
	private final MapGenScatteredFeature mScatteredFeatureGen;
	private final BiosphereOceanMonument mOceanMonumentGen;

	private BiomeGenBase[] mBiomeGens;

	private Random mRandom;

	private World mWorld;

	public BiosphereChunkGenerator(World worldIn, long seed, boolean p_i45636_4_, String p_i45636_5_) {
		super(worldIn, seed, p_i45636_4_, p_i45636_5_);
		mWorld = worldIn;
		mCaveGen = new MapGenCaves();
		mStrongholdGen = new MapGenStronghold();
		mScatteredFeatureGen = new MapGenScatteredFeature();
		mOceanMonumentGen = new BiosphereOceanMonument();
		mRandom = new Random(seed);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public Chunk provideChunk(int areaX, int areaY) {
		mRandom.setSeed((long)areaX * 341873128712L + (long)areaY * 132897987541L);
		ChunkPrimer chunkPrimer = new ChunkPrimer();

		BiosphereInfo biosphere = BiosphereChunkManager.get(mWorld).getBiosphereAtArea(areaX, areaY);
		double biomeRadiusSq = biosphere.radius * biosphere.radius;

		// load default blocks
		this.func_180518_a(areaX, areaY, chunkPrimer);

		// load biome data for generating
		mBiomeGens = mWorld.getWorldChunkManager().loadBlockGeneratorData(mBiomeGens, areaX * 16, areaY * 16, 16, 16);

		// area start position in world coordinates
		int areaWorldX = areaX * 16;
		int areaWorldZ = areaY * 16;

		// get average terrain height for this chunk in case the biosphere is new and does not yet have a height
		boolean inRootRadius = false;
		if (biosphere.height == 0) {
			int heightSum = 0;
			int numHeights = 0;
			for (int x = 0;x < 16;x++) {
				for (int z = 0;z < 16;z++) {
					if (biosphere.inRadius(areaWorldX + x, areaWorldZ + z)) {
						inRootRadius = true;
					}

					for (int y = 255; y >= 0; y--) {
						IBlockState blockState = chunkPrimer.getBlockState(x, y, z);
						if (blockState != Blocks.air.getDefaultState()) {
							heightSum += y;
							numHeights++;
							break;
						}
					}
				}
			}

			if (numHeights > 0) {
				biosphere.height = Math.max(BiosphereInfo.BIOSPHERE_MIN_HEIGHT,
				                            Math.min(BiosphereInfo.BIOSPHERE_MAX_HEIGHT,
				                                     heightSum / numHeights));
			}
		}

		// generate terrain
		this.func_180517_a(areaX, areaY, chunkPrimer, mBiomeGens);

		// generate caves
		mCaveGen.func_175792_a(this, mWorld, areaX, areaY, chunkPrimer);

		// todo ravines?

		generateFeatures(areaX, areaY, chunkPrimer, inRootRadius);

		// cut out sphere
		for (int x = 0;x < 16;x++) {
			for (int z = 0;z < 16;z++) {

				double dx = (areaX * 16 + x) - biosphere.worldCenter.getX();
				double dz = (areaY * 16 + z) - biosphere.worldCenter.getY();
				double distance2dSq = (dx * dx) + (dz * dz);

				// point not in biosphere, ensure all is air
				// terrain generation above may have added additional terrain outside of circle
				if (distance2dSq > biomeRadiusSq) {
					for (int y = 0;y < 256;y++) {
						chunkPrimer.setBlockState(x, y, z, Blocks.air.getDefaultState());
					}
					continue;
				}

				// cut out sphere itself when within biome's circle radius
				boolean underGround = false;
				for (int y = 255; y >= 0; y--) {
					double dy = y - biosphere.height;

					double distance3dSq = distance2dSq + (dy * dy);

					// get current block state
					Block block = chunkPrimer.getBlockState(x, y, z).getBlock();
					boolean isBlockSolid = block.getMaterial().blocksMovement() || block instanceof BlockFalling;

					// solid blocks found in this column, so we are now 'underground'
					underGround |= isBlockSolid;

					// cut sphere
					if (distance3dSq > biomeRadiusSq) {
						chunkPrimer.setBlockState(x, y, z, Blocks.air.getDefaultState());
					}

					// ensure sphere is closed - biosphere.radius equates to a difference of 1 in the squared distances
					else if (BiosphereInfo.DOME_ENABLED && !isBlockSolid &&
					         biomeRadiusSq - distance3dSq <= biosphere.radius * 2) {

						// close sphere below ground with stone
						// todo consider the end and the nether
						if (underGround) {
							chunkPrimer.setBlockState(x, y, z, Blocks.stone.getDefaultState());
						}

						// close sphere above ground with glass
						else {
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
		generateFeatures(areaX, areaY, null, null);
	}

	private void generateFeatures(int areaX, int areaY, ChunkPrimer chunkPrimer, Boolean overrideInRadius) {
		BiosphereInfo biosphere = BiosphereChunkManager.get(mWorld).getBiosphereAtArea(areaX,
		                                                                               areaY);

		boolean inRootRadius = false;

		// no need to recalculate if we already have; ie, did we come from provideChunk()?
		if (overrideInRadius == null) {
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {

					double distanceSq = biosphere.worldCenter.distanceSq(areaX * 16 + x, areaY * 16 + y);

					// inside inner radius
					if (distanceSq <= biosphere.radius) {
						inRootRadius = true;
					}
				}
			}
		}
		else {
			inRootRadius = overrideInRadius;
		}

		// todo mineshafts?

		// todo village

		// todo stronghold

		// todo scattered features

		if (inRootRadius) {
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

	// Populate chunk with structure gen ===============================================================================

	@SubscribeEvent
	public void onPopulateChunk(PopulateChunkEvent.Pre event) {
		mOceanMonumentGen.func_175794_a(event.world, event.rand, new ChunkCoordIntPair(event.chunkX, event.chunkZ));
	}
}
