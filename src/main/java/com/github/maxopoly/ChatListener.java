package com.github.maxopoly;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class ChatListener {

	private static Pattern hiddenOreRegex = Pattern.compile("You found a hidden ore! ([0-9]+) ([a-zA-Z ^(nearby)]+)( nearby)??");
	private LogManager logger;
	private int cachedY;

	public ChatListener(LogManager logger) {
		this.logger = logger;
	}

	@SubscribeEvent
	public void messageReceived(ClientChatReceivedEvent e) {
		String msg = e.getMessage().getUnformattedText();
		Matcher matcher = hiddenOreRegex.matcher(msg);

		if (matcher.matches()) {
			String item = matcher.group(2);
			//because I suck with regex
			if (item.endsWith(" nearby")) {
				item = item.substring(0, item.length() - " nearby".length());
			}
			String amount = matcher.group(1);
			Biome biome = OreLogger.instance.mc.theWorld.getBiome(OreLogger.instance.mc.thePlayer.getPosition());
			logger.logBreak(cachedY, item, amount, biome);
		}
	}

	@SubscribeEvent
	public void onDisconnect(ClientDisconnectionFromServerEvent event) {
		logger.writeOut();
	}
	
	@SubscribeEvent
	public void onBreakBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent event) {
		cachedY = event.getPos().getY();
	}

}
