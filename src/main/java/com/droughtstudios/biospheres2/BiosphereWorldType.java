package com.droughtstudios.biospheres2;

import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereWorldType extends WorldType {
	public static WorldType biosphereWorldType;

	public BiosphereWorldType() {
		super("Biosphere");

		if (Biospheres2.DEBUG) {
			int i;
			for (i = 0; i < worldTypes.length; i++) {
				if (this == worldTypes[i]) {
					break;
				}
			}

			WorldType oldDefault = worldTypes[0];
			worldTypes[0] = this;
			worldTypes[i] = oldDefault;
		}
	}

	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
		return new BiosphereChunkGenerator(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
	}

	@Override
	public WorldChunkManager getChunkManager(World world) {
		world.getWorldInfo().setGameType(WorldSettings.GameType.CREATIVE);
		return BiosphereChunkManager.get(world);
	}

	@Override
	public int getMinimumSpawnHeight(World world) {
		return 64;
	}

	@Override
	public boolean showWorldInfoNotice() {
		return true;
	}

	@Override
	public String getTranslateName() {
		return "Biospheres2";
	}

	@Override
	public String func_151359_c() {
		return "";
	}
}
