package com.droughtstudios.biospheres2;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Random;

/**
 * Created by Trevor on 2/8/2015.
 */
public class EmptyBiomeGenBase
		extends BiomeGenBase {

	public static int id = -1;

	public static EmptyBiomeGenBase get() {
		if (id == -1) {
			// find an unused id
			BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();

			// start at 39; current vanilla biome count is 40
			for (int i = 39;i < biomeGenArray.length;i++) {
				if (biomeGenArray[i] == null) {
					id = i;
					return new EmptyBiomeGenBase(id);
				}
			}
			return new EmptyBiomeGenBase(id, false);
		}
		return (EmptyBiomeGenBase) BiomeGenBase.getBiome(id);
	}

	public EmptyBiomeGenBase(int p_i1971_1_) {
		super(p_i1971_1_);
	}

	public EmptyBiomeGenBase(int p_i1971_1_, boolean register) {
		super(p_i1971_1_, register);
	}

	@Override
	public void genTerrainBlocks(World worldIn, Random p_180622_2_, ChunkPrimer chunkPrimer, int p_180622_4_, int p_180622_5_, double p_180622_6_) {
		int x = p_180622_5_ & 15;
		int z = p_180622_4_ & 15;
		for (int y = 255;y >= 0;y--) {
			chunkPrimer.setBlockState(x, y, z, Blocks.air.getDefaultState());
		}
	}
}
