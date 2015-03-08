package com.droughtstudios.biospheres2;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

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

	private static BiomeDictionary.Type[] biomeSubsets = new BiomeDictionary.Type[] {
			BiomeDictionary.Type.OCEAN,
			BiomeDictionary.Type.PLAINS,
			BiomeDictionary.Type.SANDY,
			BiomeDictionary.Type.MOUNTAIN,
			BiomeDictionary.Type.FOREST,
			BiomeDictionary.Type.CONIFEROUS,
			BiomeDictionary.Type.WASTELAND,
			BiomeDictionary.Type.BEACH,
			BiomeDictionary.Type.WET,
			BiomeDictionary.Type.END,
			BiomeDictionary.Type.NETHER,
			BiomeDictionary.Type.MUSHROOM
	};

	public Vec3 worldCenter;
	public float radius;
	public BiomeGenBase biome;

	public Point featurePosition = null;

	public BiosphereInfo(Point biosphereLocation, Random random) {

		// set biome ===================================================================================================

		BiomeDictionary.Type subType = biomeSubsets[random.nextInt(biomeSubsets.length)];

		Set<BiomeGenBase> biomes = new HashSet<>(Arrays.asList(BiomeDictionary.getBiomesForType(subType)));

		// if forest, filter out coniferous
		if (subType == BiomeDictionary.Type.FOREST) {
			for (BiomeGenBase biomeGenBase : BiomeDictionary.getBiomesForType(BiomeDictionary.Type.CONIFEROUS)) {
				biomes.remove(biomeGenBase);
			}
		}

		List<BiomeGenBase> validBiomes = new ArrayList<>(biomes);
		biome = validBiomes.get(random.nextInt(validBiomes.size()));

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
}
