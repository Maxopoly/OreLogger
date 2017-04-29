package com.github.maxopoly.logging;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;

public class HiddenOreSpawnManager implements LogProvider {

	private final static String SEP = ";;;";

	private List<String> logged;

	public HiddenOreSpawnManager() {
		logged = new LinkedList<String>();
	}

	public void logBreak(int y, String type, String amount, Biome biome, ItemStack tool) {
		String toolString = "";
		if (tool != null && tool.getItem() != null) {
			toolString = tool.getItem().getUnlocalizedName() + ";;" + tool.getEnchantmentTagList().toString();

		}
		logged.add((type + SEP + amount + SEP + y + SEP + biome.getBiomeName() + toolString));

	}

	@Override
	public List<String> pullPendingMessagesAndFlush() {
		List<String> temp = logged;
		logged = new LinkedList<String>();
		return temp;
	}
}
