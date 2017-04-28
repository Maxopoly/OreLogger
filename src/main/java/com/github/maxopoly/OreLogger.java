package com.github.maxopoly;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = OreLogger.MODID, version = OreLogger.VERSION)
public class OreLogger {
	public static final String MODID = "OreLogger";
	public static final String VERSION = "1.0";

	public static OreLogger instance;

	Minecraft mc;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		instance = this;
		mc = Minecraft.getMinecraft();
		LogManager logger = new LogManager();
		MinecraftForge.EVENT_BUS.register(new Listener(logger));
	}

}
