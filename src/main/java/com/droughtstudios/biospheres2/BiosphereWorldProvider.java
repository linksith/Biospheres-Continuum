package com.droughtstudios.biospheres2;

import net.minecraft.world.WorldProviderSurface;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereWorldProvider extends WorldProviderSurface {
	@Override
	public String getDimensionName() {
		return "Biospheres (Overworld)";
	}

	@Override
	public String getInternalNameSuffix() {
		return null;
	}

	@Override
	protected void registerWorldChunkManager() {
		super.registerWorldChunkManager();
		dimensionId = 0;
	}



}
