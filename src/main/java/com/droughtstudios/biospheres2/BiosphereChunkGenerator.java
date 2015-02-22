package com.droughtstudios.biospheres2;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureOceanMonument;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereChunkGenerator extends ChunkProviderGenerate {

	private final MapGenCaves mCaveGen;
	private final MapGenStronghold mStrongholdGen;
	private final MapGenScatteredFeature mScatteredFeatureGen;
	private final StructureOceanMonument mOceanMonumentGen;

	private BiomeGenBase[] mBiomeGens;

	private Random mRandom;

	private World mWorld;

	public BiosphereChunkGenerator(World worldIn, long seed, boolean p_i45636_4_, String p_i45636_5_) {
		super(worldIn, seed, p_i45636_4_, p_i45636_5_);
		mWorld = worldIn;
		mCaveGen = new MapGenCaves();
		mStrongholdGen = new MapGenStronghold();
		mScatteredFeatureGen = new MapGenScatteredFeature();
		mOceanMonumentGen = new StructureOceanMonument();
		mRandom = new Random(seed);
	}

	@Override
	public Chunk provideChunk(int areaX, int areaY) {
		mRandom.setSeed((long)areaX * 341873128712L + (long)areaY * 132897987541L);
		ChunkPrimer chunkPrimer = new ChunkPrimer();

		BiosphereInfo biosphere = BiosphereChunkManager.get(mWorld).getBiosphereAtArea(areaX,
		                                                                               areaY,
		                                                                               mRandom);
		double biomeRadiusSq = biosphere.radius * biosphere.radius;

		// load default blocks
		this.func_180518_a(areaX, areaY, chunkPrimer);

		// load biome data for generating
		mBiomeGens = mWorld.getWorldChunkManager().loadBlockGeneratorData(mBiomeGens, areaX * 16, areaY * 16, 16, 16);

		// get average terrain height for this chunk in case the biosphere is new and does not yet have a height
		if (biosphere.height == 0) {
			int heightSum = 0;
			int numHeights = 0;
			for (int x = 0;x < 16;x++) {
				for (int z = 0;z < 16;z++) {

					int primerX = (areaX * 16 + x) & 15;
					int primerZ = (areaY * 16 + z) & 15;

					for (int y = 255; y >= 0; y--) {
						IBlockState blockState = chunkPrimer.getBlockState(primerX, y, primerZ);
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

		// cut out sphere
		boolean inRootRadius = false;
		for (int x = 0;x < 16;x++) {
			for (int z = 0;z < 16;z++) {

				double dx = (areaX * 16 + x) - biosphere.worldCenter.getX();
				double dz = (areaY * 16 + z) - biosphere.worldCenter.getY();
				double distance2dSq = (dx * dx) + (dz * dz);

				int primerX = (areaX * 16 + x) & 15;
				int primerZ = (areaY * 16 + z) & 15;

				// point not in biosphere, ensure all is air
				// terrain generation above may have added additional terrain outside of circle
				if (distance2dSq > biomeRadiusSq) {
					for (int y = 0;y < 256;y++) {
						chunkPrimer.setBlockState(primerX, y, primerZ, Blocks.air.getDefaultState());
					}
					continue;
				}
				else {
					inRootRadius = true;
				}

				// cut out sphere itself when within biome's circle radius
				for (int y = 255; y >= 0; y--) {
					double dy = y - biosphere.height;

					double distance3dSq = distance2dSq + (dy * dy);

					if (distance3dSq > biomeRadiusSq) {
						chunkPrimer.setBlockState(primerX, y, primerZ, Blocks.air.getDefaultState());
					}
				}
			}
		}

		// generate caves
		mCaveGen.func_175792_a(this, mWorld, areaX, areaY, chunkPrimer);

		generateFeatures(areaX, areaY, chunkPrimer, inRootRadius);

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
		                                                                               areaY,
		                                                                               mRandom);

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

		if (inRootRadius) {
			// todo add village support (including mineshaft); currently villages are too large
			mStrongholdGen.func_175792_a(this, mWorld, areaX, areaY, chunkPrimer);
			mScatteredFeatureGen.func_175792_a(this, mWorld, areaX, areaY, chunkPrimer);
			mOceanMonumentGen.func_175792_a(this, mWorld, areaX, areaY, chunkPrimer);
		}
	}
}
