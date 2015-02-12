package com.droughtstudios.biospheres2;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureOceanMonument;

import java.util.Random;

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

		BiosphereInfo biosphere = BiosphereChunkManager.get(mWorld).getBiosphereAt(areaX,
		                                                                           areaY,
		                                                                           BiosphereInfo.BIOSPHERE_CHUNK_SIZE,
		                                                                           mRandom);

		// load default blocks
		this.func_180518_a(areaX, areaY, chunkPrimer);

		// load biome data for generating
		mBiomeGens = mWorld.getWorldChunkManager().loadBlockGeneratorData(mBiomeGens, areaX * 16, areaY * 16, 16, 16);

		// cut out the circles for the biomes
		boolean inRootRadius = false;
		for (int x = 0;x < 16;x++) {
			for (int y = 0;y < 16;y++) {

				// equation of a circle
				float a = (x + areaX * 16) - biosphere.worldCenter.x;
				float b = (y + areaY * 16) - biosphere.worldCenter.y;
				float distanceSq = (a * a) + (b * b);

				// point is outside the circle
				int index = y * 16 + x;
				if (distanceSq > biosphere.radius * biosphere.radius) {
					mBiomeGens[index] = EmptyBiomeGenBase.get();
				}
				else if (distanceSq <= biosphere.radius) {
					inRootRadius = true;
				}
			}
		}

		// generate terrain
		this.func_180517_a(areaX, areaY, chunkPrimer, mBiomeGens);

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
		BiosphereInfo biosphere = BiosphereChunkManager.get(mWorld).getBiosphereAt(areaX,
		                                                                           areaY,
		                                                                           BiosphereInfo.BIOSPHERE_CHUNK_SIZE,
		                                                                           mRandom);

		boolean inRootRadius = false;

		// no need to recalculate if we already have; ie, did we come from provideChunk()?
		if (overrideInRadius == null) {
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {

					// equation of a circle
					float a = (x + areaX * 16) - biosphere.worldCenter.x;
					float b = (y + areaY * 16) - biosphere.worldCenter.y;
					float distanceSq = (a * a) + (b * b);

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
