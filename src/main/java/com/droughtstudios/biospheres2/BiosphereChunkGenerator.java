package com.droughtstudios.biospheres2;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.ChunkProviderGenerate;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereChunkGenerator extends ChunkProviderGenerate {

	private World mWorld;

	private BiomeGenBase[] biomeGens;

	public BiosphereChunkGenerator(World worldIn, long p_i45636_2_, boolean p_i45636_4_, String p_i45636_5_) {
		super(worldIn, p_i45636_2_, p_i45636_4_, p_i45636_5_);
		mWorld = worldIn;
	}
}
