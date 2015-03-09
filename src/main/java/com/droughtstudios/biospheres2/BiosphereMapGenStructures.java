package com.droughtstudios.biospheres2;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;

/**
 * Created by Trevor on 3/2/2015.
 */
public class BiosphereMapGenStructures {

	private static boolean canSpawnCommon(int areaX, int areaY, BiosphereInfo biosphere, Class<? extends MapGenStructure> featureType) {

		if (biosphere.featurePosition != null) {
			return biosphere.featureType == featureType &&
			       biosphere.featurePosition.x == areaX && biosphere.featurePosition.y == areaY;
		}

		if (biosphere.inInnerRadius(areaX * 16, areaY * 16)) {
			biosphere.featurePosition = new Point(areaX, areaY);
			biosphere.featureType = featureType;
			return true;
		}

		return false;
	}

	/**
	 * Created by Trevor on 2/27/2015.
	 */
	public static class BiosphereOceanMonument
			extends StructureOceanMonument {

		@Override
		protected boolean canSpawnStructureAtCoords(int areaX, int areaY) {
			BiosphereChunkManager chunkManager = BiosphereChunkManager.get();
			if (chunkManager == null) return false;

			BiosphereInfo biosphere = chunkManager.getBiosphereAtArea(areaX, areaY);

			if (biosphere.biome != BiomeGenBase.deepOcean && biosphere.biome != BiomeGenBase.ocean) {
				return false;
			}

			return canSpawnCommon(areaX, areaY, biosphere, BiosphereOceanMonument.class);
		}
	}

	/**
	 * Created by Trevor on 3/2/2015.
	 */
	public static class BiosphereVillage extends MapGenVillage {

		@Override
		protected boolean canSpawnStructureAtCoords(int areaX, int areaY) {
			BiosphereChunkManager chunkManager = BiosphereChunkManager.get();
			if (chunkManager == null) return false;

			BiosphereInfo biosphere = chunkManager.getBiosphereAtArea(areaX, areaY);

			if (!MapGenVillage.villageSpawnBiomes.contains(biosphere.biome)) {
				return false;
			}

			return canSpawnCommon(areaX, areaY, biosphere, BiosphereVillage.class);
		}
	}

	public static class BiosphereStronghold extends MapGenStronghold {

		private static final int OUTER_RADIUS = 5;
		private static final int INNER_RADIUS = 2;

		private boolean mStrongholdsGenerated;

		@Override
		protected boolean canSpawnStructureAtCoords(int areaX, int areaY) {
			BiosphereChunkManager chunkManager = BiosphereChunkManager.get();
			if (chunkManager == null) return false;

			// pregen stronghold positions =============================================================================
			if (!mStrongholdsGenerated) {
				mStrongholdsGenerated = true;
				List<Integer> xCoords = new ArrayList<>();
				List<Integer> yCoords = new ArrayList<>();

				Point loc = BiosphereChunkManager.getSection(areaX, areaY, BiosphereInfo.BIOSPHERE_CHUNK_SIZE);
				for (int x = loc.x - OUTER_RADIUS;x < loc.x + OUTER_RADIUS;x++) {
					if (Math.abs(loc.x - x) >= INNER_RADIUS) {
						xCoords.add(x);
					}
				}
				for (int y = loc.y - OUTER_RADIUS;y < loc.y + OUTER_RADIUS;y++) {
					if (Math.abs(loc.y - y) >= INNER_RADIUS) {
						yCoords.add(y);
					}
				}

				// gen 3 strongholds in area
				Random random = new Random(this.worldObj.getSeed());
				for (int i = 0; i < 3;i++) {
					int x = xCoords.get(random.nextInt(xCoords.size()));
					int y = yCoords.get(random.nextInt(yCoords.size()));

					BiosphereInfo biosphere = chunkManager.getBiosphereAtCustomLocation(x, y, 1);
					biosphere.featureType = BiosphereStronghold.class;
					biosphere.featurePosition = new Point((int) biosphere.worldCenter.xCoord,
					                                      (int) biosphere.worldCenter.zCoord);
				}
			}

			// actual canSpawn =========================================================================================
			BiosphereInfo biosphere = chunkManager.getBiosphereAtArea(areaX, areaY);

			return canSpawnCommon(areaX, areaY, biosphere, BiosphereStronghold.class);
		}
	}
}
