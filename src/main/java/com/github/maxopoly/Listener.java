package com.github.maxopoly;

import com.github.maxopoly.logging.HiddenOreSpawnManager;
import com.github.maxopoly.logging.StoneBreakCounter;
import com.github.maxopoly.network.BlockBreakHandler;
import com.github.maxopoly.network.VersionChecker;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class Listener {

	private static Pattern hiddenOreRegex = Pattern
			.compile("You found a hidden ore! ([0-9]+) ([a-zA-Z ^(nearby)]+)( nearby)??");
	private HiddenOreSpawnManager hoManager;
	private StoneBreakCounter sbCounter;
	// last block the player interacted with
	private BlockPos cachedBlockPos;
	// last item the player used to interact
	private ItemStack cachedItem;
	// material of the block the player last interacted with
	private Material cachedMaterial;

	public Listener(StoneBreakCounter sbCounter, HiddenOreSpawnManager hoManager) {
		this.hoManager = hoManager;
		this.sbCounter = sbCounter;
	}

	public BlockPos getCachedBlockPos() {
		return cachedBlockPos;
	}

	public ItemStack getCachedItem() {
		return cachedItem;
	}

	public Material getCachedMaterial() {
		return cachedMaterial;
	}

	@SubscribeEvent
	public void messageReceived(ClientChatReceivedEvent e) {
		// we got a chat message, let checks whether it's from HiddenOre
		String msg = e.getMessage().getUnformattedText();
		Matcher matcher = hiddenOreRegex.matcher(msg);

		if (matcher.matches()) {
			// confirmed a HiddenOre message
			String item = matcher.group(2);
			// because I suck with regex
			if (item.endsWith(" nearby")) {
				item = item.substring(0, item.length() - " nearby".length());
			}
			String amount = matcher.group(1);
			Biome biome = OreLogger.instance.mc.theWorld.getBiome(OreLogger.instance.mc.thePlayer.getPosition());
			// send to aggregator
			hoManager.logBreak(cachedBlockPos.getY(), item, amount, biome, cachedItem);
		}
	}

	@SubscribeEvent
	public void onConnect(ClientConnectedToServerEvent event) {
		VersionChecker.checkVersion();
		// forges api doesnt offer a listener for block breaks client side only, so we resort to dirty hacks. To understand
		// how this works,
		// you need a basic understanding of netty

		// We insert our own outbound manager inbetween forge's packet handler and minecrafts packet handler. At this point
		// decryption, decompression and deserialization are done and the data being passed is an event with which we can
		// work
		event.getManager().channel().pipeline()
				.addAfter("fml:packet_handler", "processBreaks", new BlockBreakHandler(sbCounter));
	}

	@SubscribeEvent
	public void onDisconnect(ClientDisconnectionFromServerEvent event) {
		// write out all pending logs
		OreLogger.instance.getLogger().pollAndWrite();
		// reset save file so a new one is created if the player connects again
		OreLogger.instance.getLogger().resetSaveFile();
	}

	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent event) {
		cachedItem = event.getItemStack();
		cachedBlockPos = event.getPos();
		Material mat = Minecraft.getMinecraft().theWorld.getBlockState(event.getPos()).getMaterial();
		// we use this to detect the material of a broken block, so we want to ignore air, because otherwise this would
		// always be set to air at the point we handle a block break
		if (mat != Material.AIR) {
			cachedMaterial = mat;
		}
	}
}
