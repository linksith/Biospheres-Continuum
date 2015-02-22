package com.droughtstudios.biospheres2;

import java.util.Random;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereGenLayer extends GenLayer {

	private long worldGenSeed;
	private long chunkSeed;

	private Random mRandom;

	public BiosphereGenLayer(long p_i2124_1_) {
		super(p_i2124_1_);
		mRandom = new Random(p_i2124_1_);
	}

	@Override
	public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
		int[] ints = IntCache.getIntCache(areaWidth * areaHeight);

		BiosphereChunkManager chunkManager = BiosphereChunkManager.get();
		if (chunkManager == null) return ints;

		for (int x = 0;x < areaWidth;x++) {
			for (int y = 0;y < areaHeight;y++) {

				// init seed at current area
				initChunkSeed(areaX + x, areaY + y);
				mRandom.setSeed(chunkSeed);

				// get the biosphere in this area
				BiosphereInfo biosphereInfo =
						chunkManager.getBiosphereAtArea(areaX + x, areaY + y, mRandom);

				ints[y*areaWidth + x] = biosphereInfo.biome.biomeID;
			}
		}
		return ints;
	}

	/**
	 * Initialize layer's local worldGenSeed based on its own baseSeed and the world's global seed (passed in as an
	 * argument).
	 */
	@Override
	public void initWorldGenSeed(long p_75905_1_)
	{
		this.worldGenSeed = p_75905_1_;

		if (this.parent != null)
		{
			this.parent.initWorldGenSeed(p_75905_1_);
		}

		this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
		this.worldGenSeed += this.baseSeed;
		this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
		this.worldGenSeed += this.baseSeed;
		this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
		this.worldGenSeed += this.baseSeed;
	}

	/**
	 * Initialize layer's current chunkSeed based on the local worldGenSeed and the (x,z) chunk coordinates.
	 */
	@Override
	public void initChunkSeed(long p_75903_1_, long p_75903_3_)
	{
		this.chunkSeed = this.worldGenSeed;
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += p_75903_1_;
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += p_75903_3_;
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += p_75903_1_;
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += p_75903_3_;
	}
}
