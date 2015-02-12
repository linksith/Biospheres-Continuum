package com.droughtstudios.biospheres2;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereChunkManager extends WorldChunkManager {

	private List<BiomeGenBase> mSpawnBiomes;

	private Map<Point, BiosphereInfo> mBiomeMap;

	private static BiosphereChunkManager sInstance;

	public static BiosphereChunkManager get(World world) {
		if (sInstance == null) {
			sInstance = new BiosphereChunkManager(world);
		}
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
	}

	public Map<Point, BiosphereInfo> getBiomeMap() {
		return mBiomeMap;
	}

	public BiosphereInfo getBiosphereAt(int areaX, int areaY, int scale, Random random) {
		// create biosphere if it doesn't already exist
		Point chunkSection = getChunkSection(areaX, areaY, scale);
		BiosphereInfo biosphere = mBiomeMap.get(chunkSection);
		if (biosphere == null) {
			biosphere = new BiosphereInfo(chunkSection);

			biosphere.biome = mSpawnBiomes.get(random.nextInt(mSpawnBiomes.size()));

			mBiomeMap.put(chunkSection, biosphere);
		}

		return biosphere;
	}

	public static Point getChunkSection(int x, int y, int scale) {
		int sectionX = x / scale;
		int sectionY = y / scale;

		if (x < 0 && x % scale != 0) sectionX--;
		if (y < 0 && y % scale != 0) sectionY--;

		return new Point(sectionX, sectionY);
	}

	public List<BiomeGenBase> getSpawnBiomes() {
		return mSpawnBiomes;
	}

	@Override
	public boolean areBiomesViable(int p_76940_1_, int p_76940_2_, int p_76940_3_, List p_76940_4_) {
		// allow all biomes
		return true;
	}

	@Override
	public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
		EmptyBiomeGenBase.get();

		mBiomeMap = new HashMap<Point, BiosphereInfo>();

		return getBiosphereGenLayers(seed, worldType);
	}

	public GenLayer[] getBiosphereGenLayers(long seed, WorldType worldType) {
		BaseGenLayer baseLayer = new BaseGenLayer(seed);
		GenLayer genlayer = GenLayerZoom.magnify(1000L, baseLayer, 0);
		GenLayerRiverInit genlayerriverinit = new GenLayerRiverInit(100L, genlayer);
		GenLayer genlayer1 = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
		GenLayerHills genlayerhills = new GenLayerHills(1000L, baseLayer, genlayer1);
		genlayer = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
		genlayer = GenLayerZoom.magnify(1000L, genlayer, 4);
		GenLayerRiver genlayerriver = new GenLayerRiver(1L, genlayer);
		GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerriver);

		GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerhills);
		GenLayerRiverMix genlayerrivermix = new GenLayerRiverMix(100L, genlayersmooth1, genlayersmooth);
		GenLayerVoronoiZoom genlayervoronoizoom = new GenLayerVoronoiZoom(10L, genlayerrivermix);
		genlayerrivermix.initWorldGenSeed(seed);
		genlayervoronoizoom.initWorldGenSeed(seed);

		TrackPositionGenLayer trackPositionRiverLayer = new TrackPositionGenLayer(1, genlayerrivermix, baseLayer);

		return new GenLayer[]{
			trackPositionRiverLayer,
			new TrackPositionGenLayer(4, genlayervoronoizoom, baseLayer),
			trackPositionRiverLayer
		};
	}

	private class BaseGenLayer extends GenLayer {

		private Point mCurrentArea;

		public BaseGenLayer(long p_i2124_1_) {
			super(p_i2124_1_);
		}

		public void setNextArea(Point area) {
			mCurrentArea = area;
		}

		@Override
		public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
			if (mSpawnBiomes == null) {
				BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
				mSpawnBiomes = new ArrayList<BiomeGenBase>(biomeGenArray.length);
				for (BiomeGenBase biomeGenBase : biomeGenArray) {
					if (biomeGenBase != null) mSpawnBiomes.add(biomeGenBase);
				}
			}

			Point cachePoint = mCurrentArea != null ? mCurrentArea : new Point(areaX, areaY);
			BiosphereInfo biosphere = mBiomeMap.get(cachePoint);
			int biomeId = biosphere == null ? BiomeGenBase.birchForest.biomeID : biosphere.biome.biomeID;

			int[] ints = IntCache.getIntCache(areaWidth * areaHeight);
			for (int x = 0;x < areaWidth;x++) {
				for (int y = 0;y < areaHeight;y++) {
					ints[y*areaWidth + x] = biomeId;
				}
			}
			return ints;
		}
	}

	private class TrackPositionGenLayer extends GenLayer {

		private BaseGenLayer mBaseLayer;
		private int mScale;

		public TrackPositionGenLayer(int scale, GenLayer parent, BaseGenLayer baseLayer) {
			// seed doesn't matter, we aren't using it
			super(0);
			mScale = scale;
			mBaseLayer = baseLayer;
			this.parent = parent;
		}

		@Override
		public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
			// area should not account for individual blocks, and the base zoom is 4
			mBaseLayer.setNextArea(getChunkSection(areaX, areaY, mScale * 16 * (BiosphereInfo.BIOSPHERE_CHUNK_SIZE / 4)));
			return parent.getInts(areaX, areaY, areaWidth, areaHeight);
		}
	}
}
