package com.droughtstudios.biospheres2;

import net.minecraft.util.BlockPos;
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

//	@Override
//	public IChunkProvider createChunkGenerator() {
//		return new BiosphereChunkGenerator();
//	}

	@Override
	protected void registerWorldChunkManager() {
		worldChunkMgr = new BiosphereChunkManager(worldObj);
		dimensionId = 0;
	}

	@Override
	public BlockPos getRandomizedSpawnPoint() {
		// todo
		return super.getRandomizedSpawnPoint();
	}

	@Override
	public BlockPos getSpawnPoint() {
		// todo
		return super.getSpawnPoint();
	}


}
