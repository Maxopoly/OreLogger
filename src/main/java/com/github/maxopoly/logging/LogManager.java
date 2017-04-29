package com.github.maxopoly.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;

public class LogManager {

	private final static int saveDelay = 300; // every 5 minutes

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private File logFile;
	private List<LogProvider> logProvider;

	public LogManager() {
		File minecraftFolder = Minecraft.getMinecraft().mcDataDir;
		File saveFolder = new File(minecraftFolder, "loggedOres");
		saveFolder.mkdir();
		String playerName = Minecraft.getMinecraft().getSession().getUsername();
		String serverIP = Minecraft.getMinecraft().getCurrentServerData().serverIP;
		logFile = new File(saveFolder, String.valueOf(playerName + "_" + serverIP + "_" + System.currentTimeMillis())
				+ ".txt");
		try {
			logFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create log file, shutting down");
			// TODO properly shutdown instead of just never clearing out cached logs
			e.printStackTrace();
			return;
		}
		Runnable saver = new Runnable() {

			@Override
			public void run() {
				pollAndWrite();
			}
		};
		scheduler.scheduleAtFixedRate(saver, saveDelay, saveDelay, TimeUnit.SECONDS);
	}

	public synchronized void pollAndWrite() {
		List<String> toSave = new LinkedList<String>();
		for (LogProvider provider : logProvider) {
			toSave.addAll(provider.pullPendingMessagesAndFlush());
		}
		saveToFile(logFile, toSave);
	}

	public synchronized void registerLogProvider(LogProvider provider) {
		logProvider.add(provider);
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
