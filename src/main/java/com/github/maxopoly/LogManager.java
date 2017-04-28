package com.github.maxopoly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.minecraft.world.biome.Biome;

public class LogManager {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static final int saveDelay = 300; // every 5 minutes

	private List<String> contentToSave;
	private File logFile;

	public LogManager() {
		contentToSave = new LinkedList<String>();
		File minecraftFolder = OreLogger.instance.mc.mcDataDir;
		File saveFolder = new File(minecraftFolder, "loggedOres");
		saveFolder.mkdir();
		String playerName = OreLogger.instance.mc.getSession().getUsername();
		logFile = new File(saveFolder, String.valueOf(playerName + "_" + System.currentTimeMillis()) + ".txt");
		try {
			logFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create log file, shutting down");
			e.printStackTrace();
			return;
		}
		Runnable saver = new Runnable() {

			@Override
			public void run() {
				writeOut();
			}
		};
		final ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(saver, saveDelay, saveDelay, TimeUnit.SECONDS);
	}

	public void logBreak(int y, String type, String amount, Biome biome) {
		contentToSave.add(type + ";;" + amount + ";;" + y + ";;" + biome.getBiomeName() + "\n");
	}

	public void writeOut() {
		synchronized (contentToSave) {
			saveToFile(logFile, contentToSave);
			contentToSave.clear();
		}
	}

	private static void saveToFile(File file, List<String> toSave) {
		if (toSave.size() == 0) {
			return;
		}
		try {
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter writer = new PrintWriter(bw);
			for (String line : toSave) {
				writer.write(line);
				System.out.println("Writing " + line + " to file");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Failed to save to file");
			e.printStackTrace();
		}
	}
}
