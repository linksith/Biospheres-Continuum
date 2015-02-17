package com.droughtstudios.biospheres2;

import net.minecraft.world.biome.BiomeGenBase;

import java.awt.*;

/**
 * Created by Trevor on 2/11/2015.
 */
public class BiosphereInfo {

	// biospheres takes SIZExSIZE chunks
	public static final int BIOSPHERE_CHUNK_SIZE = 16;
	public static final int BIOSPHERE_RADIUS_CHUNKS = 5;
	public static final int BIOSPHERE_MAX_HEIGHT = 128;
	public static final int BIOSPHERE_MIN_HEIGHT = BIOSPHERE_RADIUS_CHUNKS * BIOSPHERE_CHUNK_SIZE;

	public Point.Float worldCenter;
	public float radius;
	public BiomeGenBase biome;
	public int height = 0;

	public BiosphereInfo(Point biosphereLocation) {
		worldCenter = new Point.Float(((float)biosphereLocation.x + 0.5f) * BIOSPHERE_CHUNK_SIZE * 16f,
		                              ((float)biosphereLocation.y + 0.5f) * BIOSPHERE_CHUNK_SIZE * 16f);
		radius = BIOSPHERE_RADIUS_CHUNKS * BIOSPHERE_CHUNK_SIZE;
	}
}
