package com.github.maxopoly.logging;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.biome.Biome;

public class HiddenOreSpawnManager implements LogProvider {

	private final static String SEP = ";;;";

	private List<String> logged;

	public HiddenOreSpawnManager() {
		logged = new LinkedList<String>();
	}

	public void logBreak(int y, String type, String amount, Biome biome, ItemStack tool) {
		String toolString = createToolString(tool);
		logged.add((type + SEP + amount + SEP + y + SEP + biome.getBiomeName() + SEP + toolString));

	}

	private String createToolString(ItemStack tool) {
		String toolString = "";
		if (tool == null || tool.getItem() == null) {
			return toolString;
		}
		toolString += tool.getItem().getUnlocalizedName();
		toolString += ";;";
		if (tool.getEnchantmentTagList() != null) {
			toolString += tool.getEnchantmentTagList().toString();
		}
		if (tool.hasTagCompound() && tool.getTagCompound().hasKey("display")) {
			NBTTagCompound displayTag = tool.getTagCompound().getCompoundTag("display");
			if (displayTag.getTagId("Lore") == 9) {
				NBTTagList nbttaglist3 = displayTag.getTagList("Lore", 8);
				if (!nbttaglist3.hasNoTags()) {
					for (int l1 = 0; l1 < nbttaglist3.tagCount(); ++l1) {
						toolString += ";;";
						toolString += nbttaglist3.getStringTagAt(l1);
					}
				}
			}
		}
		return toolString;
	}

	@Override
	public List<String> pullPendingMessagesAndFlush() {
		List<String> temp = logged;
		logged = new LinkedList<String>();
		return temp;
	}
}
