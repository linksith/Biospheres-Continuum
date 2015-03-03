package com.droughtstudios.biospheres2;

import java.awt.Point;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureOceanMonument;

/**
 * Created by Trevor on 2/27/2015.
 */
public class BiosphereOceanMonument
		extends StructureOceanMonument {

	@Override
	protected boolean canSpawnStructureAtCoords(int areaX, int areaY) {
		BiosphereChunkManager chunkManager = BiosphereChunkManager.get();
		if (chunkManager == null) return false;

		BiosphereInfo biosphere = chunkManager.getBiosphereAtArea(areaX, areaY);

		if (biosphere.featurePosition != null) {
			return biosphere.featurePosition.x == areaX && biosphere.featurePosition.y == areaY;
		}

		if ((biosphere.biome == BiomeGenBase.deepOcean || biosphere.biome == BiomeGenBase.ocean) &&
		    biosphere.inInnerRadius(areaX * 16, areaY * 16)) {
			biosphere.featurePosition = new Point(areaX, areaY);
			return true;
		}
		return false;
	}
}
