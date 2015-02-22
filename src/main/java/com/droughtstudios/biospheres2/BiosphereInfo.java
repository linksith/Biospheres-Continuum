package com.droughtstudios.biospheres2;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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

	public Point.Float worldCenter;
	public float radius;
	public BiomeGenBase biome;
	public int height = 0;

	public static List<BiomeGenBase> validBiomes;

	public BiosphereInfo(Point biosphereLocation, Random random) {
		radius = BIOSPHERE_RADIUS_CHUNKS * 16;

		// spawn area is subsquare of total area; subsquare is -radius on each side
		int spawnAreaSize = (BIOSPHERE_CHUNK_SIZE * 16) - 2 * (int)radius;
		float spawnX = radius + random.nextInt(spawnAreaSize);
		float spawnY = radius + random.nextInt(spawnAreaSize);

		worldCenter = new Point.Float((float)biosphereLocation.x * BIOSPHERE_CHUNK_SIZE * 16f + spawnX,
		                              (float)biosphereLocation.y * BIOSPHERE_CHUNK_SIZE * 16f + spawnY);

		if (validBiomes == null) initValidBiomes();

		biome = validBiomes.get(random.nextInt(validBiomes.size()));
	}

	private void initValidBiomes() {
		Set<BiomeGenBase> biomes = new HashSet<>();

		BiomeDictionary.Type[] types = BiomeDictionary.Type.values();
		for (BiomeDictionary.Type type : types) {
			if (type != BiomeDictionary.Type.RIVER) {
				for (BiomeGenBase biomeGenBase : BiomeDictionary.getBiomesForType(type)) {
					biomes.add(biomeGenBase);
				}
			}
		}

		validBiomes = new ArrayList<>(biomes);
	}
}
