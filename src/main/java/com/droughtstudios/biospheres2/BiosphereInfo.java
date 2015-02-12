package com.droughtstudios.biospheres2;

import net.minecraft.world.biome.BiomeGenBase;

import java.awt.*;

/**
 * Created by Trevor on 2/11/2015.
 */
public class BiosphereInfo {

	// biospheres takes SIZExSIZE chunks
	public static final int BIOSPHERE_CHUNK_SIZE = 16;

	public Point.Float worldCenter;
	public float radius;
	public BiomeGenBase biome;

	public BiosphereInfo(Point biosphereLocation) {
		worldCenter = new Point.Float(((float)biosphereLocation.x + 0.5f) * BIOSPHERE_CHUNK_SIZE * 16f,
		                              ((float)biosphereLocation.y + 0.5f) * BIOSPHERE_CHUNK_SIZE * 16f);
		radius = 6 * BIOSPHERE_CHUNK_SIZE;
	}
}
