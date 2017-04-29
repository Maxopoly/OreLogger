package com.github.maxopoly;

import com.github.maxopoly.logging.HiddenOreSpawnManager;
import com.github.maxopoly.logging.LogManager;
import com.github.maxopoly.logging.StoneBreakCounter;
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

	private Listener listener;
	private LogManager logger;

	Minecraft mc;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		instance = this;
		mc = Minecraft.getMinecraft();
		logger = new LogManager();
		HiddenOreSpawnManager hoManager = new HiddenOreSpawnManager();
		logger.registerLogProvider(hoManager);
		StoneBreakCounter stoneCounter = new StoneBreakCounter();
		logger.registerLogProvider(stoneCounter);
		listener = new Listener(stoneCounter, hoManager);
		MinecraftForge.EVENT_BUS.register(listener);
	}

	public Listener getListener() {
		return listener;
	}

	public LogManager getLogger() {
		return logger;
	}

}
