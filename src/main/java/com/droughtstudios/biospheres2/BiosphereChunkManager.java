package com.droughtstudios.biospheres2;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerHills;
import net.minecraft.world.gen.layer.GenLayerRiver;
import net.minecraft.world.gen.layer.GenLayerRiverInit;
import net.minecraft.world.gen.layer.GenLayerRiverMix;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.gen.layer.IntCache;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereChunkManager extends WorldChunkManager {

	private Map<Point, BiosphereInfo> mBiomeMap;

	private static BiosphereChunkManager sInstance;

	public static BiosphereChunkManager get(World world) {
		if (sInstance == null) {
			sInstance = new BiosphereChunkManager(world);
		}
		return sInstance;
	}

	public static BiosphereChunkManager get() {
		return sInstance;
	}

	public BiosphereChunkManager() {
		super();
	}

	public BiosphereChunkManager(long seed, WorldType worldType, String settingsJson) {
		super(seed, worldType, settingsJson);
	}

	public BiosphereChunkManager(World worldIn) {
		super(worldIn);
		EmptyBiomeGenBase.get();

		mBiomeMap = new HashMap<>();
	}

	public Map<Point, BiosphereInfo> getBiomeMap() {
		return mBiomeMap;
	}

	public BiosphereInfo getBiosphereAtWorldPos(int blockPosX, int blockPosY) {
		return getBiosphereAtCustomLocation(blockPosX, blockPosY, BiosphereInfo.BIOSPHERE_CHUNK_SIZE * 16);
	}

	public BiosphereInfo getBiosphereAtArea(int areaX, int areaY) {
		return getBiosphereAtCustomLocation(areaX, areaY, BiosphereInfo.BIOSPHERE_CHUNK_SIZE);
	}

	public BiosphereInfo getBiosphereAtCustomLocation(int x, int y, int scale) {
		// create biosphere if it doesn't already exist
		Point chunkSection = getSection(x, y, scale);
		BiosphereInfo biosphere = mBiomeMap.get(chunkSection);
		if (biosphere == null) {
			biosphere = new BiosphereInfo(chunkSection, new Random((long)x * 341873128712L + (long)y * 132897987541L));
			mBiomeMap.put(chunkSection, biosphere);
		}

		return biosphere;
	}

	public static Point getSection(int x, int y, int sectionSize) {
		int sectionX = x / sectionSize;
		int sectionY = y / sectionSize;

		if (x < 0 && x % sectionSize != 0) sectionX--;
		if (y < 0 && y % sectionSize != 0) sectionY--;

		return new Point(sectionX, sectionY);
	}

	@Override
	public boolean areBiomesViable(int p_76940_1_, int p_76940_2_, int p_76940_3_, List p_76940_4_) {
		return true;
	}

	@Override
	public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
		return getBiosphereGenLayers(seed, worldType);
	}

	public GenLayer[] getBiosphereGenLayers(long seed, WorldType worldType) {
		// biomes
		GenLayer baseLayer = new BiosphereGenLayer(seed);

		// zoom for calculation discrepancy between hills & rivers gen layers
		GenLayer genlayer = new GenLayerCopyZoom(1000L, baseLayer);
		genlayer = new GenLayerCopyZoom(1000L, genlayer);

		// river & hill generation
		GenLayerRiverInit genlayerriverinit = new GenLayerRiverInit(100L, baseLayer);
		GenLayer genlayer1 = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
		GenLayerHills genlayerhills = new GenLayerHills(1000L, genlayer, genlayer1);
		baseLayer = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
		baseLayer = GenLayerZoom.magnify(1000L, baseLayer, 4);
		GenLayerRiver genlayerriver = new GenLayerRiver(1L, baseLayer);
		GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerriver);

		GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerhills);
		GenLayerRiverMix genlayerrivermix = new GenLayerRiverMix(100L, genlayersmooth1, genlayersmooth);
		GenLayerVoronoiZoom genlayervoronoizoom = new GenLayerVoronoiZoom(10L, genlayerrivermix);
		genlayerrivermix.initWorldGenSeed(seed);
		genlayervoronoizoom.initWorldGenSeed(seed);

		return new GenLayer[] {
				genlayerrivermix,
				genlayervoronoizoom,
				genlayerrivermix
		};
	}

	private class GenLayerCopyZoom extends GenLayer {

		public GenLayerCopyZoom(long p_i2125_1_, GenLayer parent) {
			super(p_i2125_1_);
			this.parent = parent;
		}

		@Override
		public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
			int sampleWidth = (areaWidth >> 1) + 1;
			int sampleHeight = (areaHeight >> 1) + 1;

			int[] ints = parent.getInts(areaX >> 1, areaY >> 1, sampleWidth, sampleHeight);
			int[] ret = IntCache.getIntCache(areaWidth * areaHeight);

			for (int x = 0;x < areaWidth;x++) {
				for (int y = 0;y < areaHeight;y++) {
					ret[y*areaWidth + x] = ints[(y >> 1)*sampleWidth + (x >> 1)];
				}
			}

			return ret;
		}
	}
}
