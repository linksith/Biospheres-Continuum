package com.droughtstudios.biospheres2;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereChunkManager extends WorldChunkManager {

	private List<BiomeGenBase> mSpawnBiomes;

	private Map<Point, Integer> mBiomeMap;

	public BiosphereChunkManager() {
		super();
	}

	public BiosphereChunkManager(long seed, WorldType worldType, String settingsJson) {
		super(seed, worldType, settingsJson);
	}

	public BiosphereChunkManager(World worldIn) {
		super(worldIn);
	}

	@Override
	public boolean areBiomesViable(int p_76940_1_, int p_76940_2_, int p_76940_3_, List p_76940_4_) {
		// allow all biomes
		return true;
	}

	@Override
	public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
		GenLayer[] genLayers = super.getModdedBiomeGenerators(worldType, seed, original);

		// ensure empty biome is registered
//		EmptyBiomeGenBase.get();

		mBiomeMap = new HashMap<Point, Integer>();

		return getBiosphereGenLayers(seed, worldType);
	}

	public GenLayer[] getBiosphereGenLayers(long seed, WorldType worldType) {
//		mBaseGenLayer = new BaseGenLayer(1L);
//		GenLayerFuzzyZoom genlayerfuzzyzoom = new GenLayerFuzzyZoom(2000L, mBaseGenLayer);
//		GenLayerAddIsland genlayeraddisland = new GenLayerAddIsland(1L, genlayerfuzzyzoom);
//		GenLayerZoom genlayerzoom = new GenLayerZoom(2001L, genlayeraddisland);
//		genlayeraddisland = new GenLayerAddIsland(2L, genlayerzoom);
//		genlayeraddisland = new GenLayerAddIsland(50L, genlayeraddisland);
//		genlayeraddisland = new GenLayerAddIsland(70L, genlayeraddisland);
//		GenLayerRemoveTooMuchOcean genlayerremovetoomuchocean = new GenLayerRemoveTooMuchOcean(2L, genlayeraddisland);
//		GenLayerAddSnow genlayeraddsnow = new GenLayerAddSnow(2L, genlayerremovetoomuchocean);
//		genlayeraddisland = new GenLayerAddIsland(3L, genlayeraddsnow);
//		GenLayerEdge genlayeredge = new GenLayerEdge(2L, genlayeraddisland, GenLayerEdge.Mode.COOL_WARM);
//		genlayeredge = new GenLayerEdge(2L, genlayeredge, GenLayerEdge.Mode.HEAT_ICE);
//		genlayeredge = new GenLayerEdge(3L, genlayeredge, GenLayerEdge.Mode.SPECIAL);
//		genlayerzoom = new GenLayerZoom(2002L, genlayeredge);
//		genlayerzoom = new GenLayerZoom(2003L, genlayerzoom);
//		genlayeraddisland = new GenLayerAddIsland(4L, genlayerzoom);
//		GenLayerAddMushroomIsland genlayeraddmushroomisland = new GenLayerAddMushroomIsland(5L, genlayeraddisland);
//		GenLayerDeepOcean genlayerdeepocean = new GenLayerDeepOcean(4L, genlayeraddmushroomisland);
//		GenLayer genlayer2 = GenLayerZoom.magnify(1000L, genlayerdeepocean, 0);
//
		BaseGenLayer baseLayer = new BaseGenLayer(seed);
		GenLayer genlayer = GenLayerZoom.magnify(1000L, baseLayer, 0);
		GenLayerRiverInit genlayerriverinit = new GenLayerRiverInit(100L, genlayer);
//		GenLayer genlayerbiomeedge = p_180781_2_.getBiomeLayer(p_180781_0_, genlayer2, p_180781_3_);
		GenLayer genlayer1 = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
		GenLayerHills genlayerhills = new GenLayerHills(1000L, baseLayer, genlayer1);
		genlayer = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
		genlayer = GenLayerZoom.magnify(1000L, genlayer, 4);
		GenLayerRiver genlayerriver = new GenLayerRiver(1L, genlayer);
		GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerriver);

		GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerhills);
		GenLayerRiverMix genlayerrivermix = new GenLayerRiverMix(100L, genlayersmooth1, genlayersmooth);
		TrackPositionGenLayer trackGenLayer = new TrackPositionGenLayer(genlayerrivermix, baseLayer);
		GenLayerVoronoiZoom genlayervoronoizoom = new GenLayerVoronoiZoom(10L, trackGenLayer);
		genlayerrivermix.initWorldGenSeed(seed);
		genlayervoronoizoom.initWorldGenSeed(seed);

		return new GenLayer[] {trackGenLayer, genlayervoronoizoom, trackGenLayer};
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

//			int ax = areaX / 16;
//			int ay = areaY / 16;
//
//			if (areaX < 0) ax--;
//			if (areaY < 0) ay--;

//			StringBuilder sb = new StringBuilder()
//                    .append("Area is (").append(areaX).append(',').append(areaY).append(')')
//					.append("-> maps to (").append(ax).append(',').append(ay).append(')');
//
//			FMLLog.info(sb.toString());

			Point cachePoint = mCurrentArea != null ? mCurrentArea : new Point(areaX, areaY);
			Integer biome = mBiomeMap.get(cachePoint);
			if (biome == null) {
				biome = mSpawnBiomes.get(nextInt(mSpawnBiomes.size())).biomeID;
				mBiomeMap.put(cachePoint, biome);
			}

			int[] ints = IntCache.getIntCache(areaWidth * areaHeight);
			for (int x = 0;x < areaWidth;x++) {
				for (int y = 0;y < areaHeight;y++) {
					ints[y*areaWidth + x] = biome;
				}
			}
			return ints;
		}
	}

	private class TrackPositionGenLayer extends GenLayer {

		private BaseGenLayer mBaseLayer;

		public TrackPositionGenLayer(GenLayer parent, BaseGenLayer baseLayer) {
			// seed doesn't matter, we aren't using it
			super(0);
			mBaseLayer = baseLayer;
			this.parent = parent;
		}

		@Override
		public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
			int x = areaX / 16;
			int y = areaY / 16;
			if (areaX < 0) x--;
			if (areaY < 0) y--;

			mBaseLayer.setNextArea(new Point(x, y));
			return parent.getInts(areaX, areaY, areaWidth, areaHeight);
		}
	}
}
