package com.droughtstudios.biospheres2;

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

		// unregister old overworld provider and register our own
//		DimensionManager.unregisterProviderType(0);
//		DimensionManager.registerProviderType(0, BiosphereWorldProvider.class, true);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
