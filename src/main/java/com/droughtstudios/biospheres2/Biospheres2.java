package com.droughtstudios.biospheres2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by Trevor on 2/8/2015.
 */

@Mod(modid = "Biospheres2", name = "Biospheres2", version = "0.1")
public class Biospheres2 {

	public static final boolean DEBUG = true;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {

	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		BiosphereWorldType.biosphereWorldType = new BiosphereWorldType();
		BlockDome.register();

		Item item = Item.getItemFromBlock(BlockDome.blockDome);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation("biospheres2:glass_dome", "inventory"));
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
