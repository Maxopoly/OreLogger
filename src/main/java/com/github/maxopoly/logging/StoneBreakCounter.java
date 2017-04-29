package com.github.maxopoly.logging;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class StoneBreakCounter implements LogProvider {

	private final static int positionsCachedMaximum = 20;
	private final static String SEP = ";;;";

	private List<BlockPos> cachedPositions;
	private Map<String, int[]> blocksMinedByYLevelOrderedByBiome;

	public StoneBreakCounter() {
		blocksMinedByYLevelOrderedByBiome = new TreeMap<String, int[]>();
		cachedPositions = new LinkedList<BlockPos>();
	}

	public synchronized void breakOccured(BlockPos position) {
		if (cachedPositions.contains(position)) {
			// The same block is being broken repeatedly. We do not want to log it twice
			System.out.println("Cancelling logging of stone, because position was already logged");
			// TODO test this
			return;
		}
		// cache positions and remove oldest cached position if needed
		cachedPositions.add(position);
		if (cachedPositions.size() > positionsCachedMaximum) {
			cachedPositions.remove(0);
		}
		String biomeName = Minecraft.getMinecraft().theWorld.getBiome(Minecraft.getMinecraft().thePlayer.getPosition())
				.getBiomeName();
		int[] yLevels = blocksMinedByYLevelOrderedByBiome.get(biomeName);
		if (yLevels == null) {
			// assume maximum y level is always 255
			yLevels = new int[256];
			blocksMinedByYLevelOrderedByBiome.put(biomeName, yLevels);
		}
		yLevels[position.getY()]++;
	}

	@Override
	public synchronized List<String> pullPendingMessagesAndFlush() {
		List<String> result = new LinkedList<String>();
		for (Entry<String, int[]> entry : blocksMinedByYLevelOrderedByBiome.entrySet()) {
			for (int i = 0; i < entry.getValue().length; i++) {
				if (entry.getValue()[i] != 0) {
					// player actually broke at this y-level, we only want to log in this case
					// example format is 'STONEBREAK;;;12;;;230;;;PLAINS' for 230 blocks broken at y 12 in plains
					result.add("STONEBREAK" + SEP + i + SEP + entry.getValue()[i] + SEP + entry.getKey());
				}
			}
		}
		blocksMinedByYLevelOrderedByBiome.clear();
		return result;
	}
}
