package com.droughtstudios.biospheres2;

import java.awt.Point;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Created by Trevor on 2/11/2015.
 */
public class BiosphereInfo {

	// biospheres takes SIZExSIZE chunks
	public static final int BIOSPHERE_CHUNK_SIZE = 16;
	public static final int BIOSPHERE_RADIUS_CHUNKS = 5;
	public static final int BIOSPHERE_MAX_HEIGHT = 128;
	public static final int BIOSPHERE_MIN_HEIGHT = BIOSPHERE_RADIUS_CHUNKS * BIOSPHERE_CHUNK_SIZE;

	public static boolean DOME_ENABLED = true;

	private static WeightedMap<WeightedMap<BiomeGenBase>> biomeTypes;

	public Vec3 worldCenter;
	public float radius;
	public BiomeGenBase biome;

	public Point featurePosition = null;

	public BiosphereInfo(Point biosphereLocation, Random random) {
		// set biome ===================================================================================================
		genWeightedBiomeList();

		biome = biomeTypes.getRandom(random).getRandom(random);

		// set world location ==========================================================================================

		radius = BIOSPHERE_RADIUS_CHUNKS * 16;

		// spawn area is subsquare of total area; subsquare is -radius on each side
		int spawnAreaSize = (BIOSPHERE_CHUNK_SIZE * 16) - 2 * (int)radius;
		float spawnX = radius + random.nextInt(spawnAreaSize);
		float spawnY = radius + random.nextInt(spawnAreaSize);

		double biomeMinHeight = 64.0 + 32.0 * biome.minHeight;
		double biomeMaxHeight = 64.0 + 32.0 * (biome.minHeight + biome.maxHeight);
		double biomeHeight = Math.max(BIOSPHERE_MIN_HEIGHT,
		                              Math.min(BIOSPHERE_MAX_HEIGHT,
		                                       biomeMinHeight + ((biomeMaxHeight - biomeMinHeight) / 2.0)));

		worldCenter = new Vec3(biosphereLocation.x * BIOSPHERE_CHUNK_SIZE * 16.0 + spawnX,
		                       biomeHeight,
		                       biosphereLocation.y * BIOSPHERE_CHUNK_SIZE * 16.0 + spawnY);
	}

	public boolean inRadius(int worldPosX, int worldPosY, int worldPosZ) {
		double dx = worldPosX - worldCenter.xCoord;
		double dy = worldPosY - worldCenter.yCoord;
		double dz = worldPosZ - worldCenter.zCoord;
		return ((dx * dx) + (dy * dy) + (dz * dz)) < radius * radius;
	}

	public boolean inRadius(int worldPosX, int worldPosY) {
		double dx = worldPosX - worldCenter.xCoord;
		double dz = worldPosY - worldCenter.zCoord;
		return ((dx * dx) + (dz * dz)) < radius * radius;
	}

	public boolean inInnerRadius(int worldPosX, int worldPosY) {
		double dx = worldPosX - worldCenter.xCoord;
		double dz = worldPosY - worldCenter.zCoord;
		return ((dx * dx) + (dz * dz)) < radius;
	}

	private void genWeightedBiomeList() {
		if (biomeTypes != null) return;

		// todo don't use biomes directly, make more extendable with other mods
		WeightedMap<BiomeGenBase> ocean = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.ocean, 0.4)
				.add(BiomeGenBase.deepOcean, 0.4)
				.add(BiomeGenBase.frozenOcean, 0.2);

		WeightedMap<BiomeGenBase> plains = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.plains, 0.5)
				.add(BiomeGenBase.savanna, 0.3)
				.add(BiomeGenBase.savannaPlateau, 0.2);

		WeightedMap<BiomeGenBase> sandy = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.desert, 0.3)
				.add(BiomeGenBase.desertHills, 0.2)
				.add(BiomeGenBase.mesa, 0.25)
				.add(BiomeGenBase.mesaPlateau, 0.15)
				.add(BiomeGenBase.mesaPlateau_F, 0.1);

		WeightedMap<BiomeGenBase> mountain = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.extremeHills, 0.4)
				.add(BiomeGenBase.iceMountains, 0.1)
				.add(BiomeGenBase.extremeHillsEdge, 0.2)
				.add(BiomeGenBase.extremeHillsPlus, 0.3);

		WeightedMap<BiomeGenBase> forest = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.forest, 0.3)
				.add(BiomeGenBase.forestHills, 0.2)
				.add(BiomeGenBase.birchForest, 0.2)
				.add(BiomeGenBase.birchForestHills, 0.1)
				.add(BiomeGenBase.roofedForest, 0.2);

		WeightedMap<BiomeGenBase> coniferous = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.taiga, 0.2)
				.add(BiomeGenBase.taigaHills, 0.2)
				.add(BiomeGenBase.coldTaiga, 0.2)
				.add(BiomeGenBase.coldTaigaHills, 0.1)
				.add(BiomeGenBase.megaTaiga, 0.2)
				.add(BiomeGenBase.megaTaigaHills, 0.1);

		WeightedMap<BiomeGenBase> wasteland = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.icePlains, 1);

		WeightedMap<BiomeGenBase> beach = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.beach, 0.6)
				.add(BiomeGenBase.stoneBeach, 0.3)
				.add(BiomeGenBase.coldBeach, 0.1);

		WeightedMap<BiomeGenBase> wet = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.swampland, 0.4)
				.add(BiomeGenBase.jungle, 0.3)
				.add(BiomeGenBase.jungleHills, 0.2)
				.add(BiomeGenBase.jungleEdge, 0.1);

		WeightedMap<BiomeGenBase> mushroom = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.mushroomIsland, 0.8)
				.add(BiomeGenBase.mushroomIslandShore, 0.2);

		biomeTypes = new WeightedMap<WeightedMap<BiomeGenBase>>()
				.add(ocean, 0.1)
				.add(plains, 0.15)
				.add(sandy, 0.1)
				.add(mountain, 0.2)
				.add(forest, 0.1)
				.add(coniferous, 0.1)
				.add(wasteland, 0.05)
				.add(beach, 0.05)
				.add(wet, 0.1)
				.add(mushroom, 0.05);
	}

	private class WeightedMap<T> {
		private final NavigableMap<Double, T> mWeightedmap = new TreeMap<>();
		private double total = 0;

		public WeightedMap<T> add(T item, double weight) {
			mWeightedmap.put(total, item);
			total += weight;
			return this;
		}

		public T getRandom(Random random) {
			return mWeightedmap.floorEntry(random.nextDouble()).getValue();
		}
	}
}
