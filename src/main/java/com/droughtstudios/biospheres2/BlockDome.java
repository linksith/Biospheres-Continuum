package com.droughtstudios.biospheres2;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Trevor on 2/22/2015.
 */
public class BlockDome extends BlockGlass {

	private static boolean registered = false;

	public static Block blockDome;

	public static void register() {
		if (!registered) {
			registered = true;
			blockDome = GameRegistry.registerBlock(new BlockDome(), "glass_dome");
		}
	}

	protected BlockDome() {
		super(Material.glass, false);

		setHardness(1.0f);
		setStepSound(soundTypeGlass);
		setUnlocalizedName("glassDome");
	}

	@Override
	public boolean isFoliage(IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
}
