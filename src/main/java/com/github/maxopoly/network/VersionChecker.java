package com.github.maxopoly.network;

import com.github.maxopoly.OreLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;

public class VersionChecker implements Runnable {

	private static final String versionFilePath = "https://raw.githubusercontent.com/Maxopoly/OreLogger/master/src/main/resources/releaseVersion.txt";

	public static void checkVersion() {
		new Thread(new VersionChecker()).start();
	}

	@Override
	public void run() {
		String version = null;
		try {
			version = retrieve(versionFilePath);
		} catch (MalformedURLException e) {
			System.err.println("Failed to retrieve version");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Failed to retrieve version");
			e.printStackTrace();
		}
		System.out.println(version);
		if (version != null && !version.equals(OreLogger.VERSION)) {
			EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
			if (player != null) {
				player.addChatComponentMessage(new TextComponentString(
						"OreLogger has a new version available. You are running " + OreLogger.VERSION + ", latest is " + version));
			}
		}
	}

	// everything below this is a modified version of code linked here
	// http://stackoverflow.com/questions/1427508/how-to-download-a-file-over-http-and-store-its-content-in-a-string-in-java

	private InputStream prepareInputStream(String urlToRetrieve) throws IOException {
		URL url = new URL(urlToRetrieve);
		URLConnection uc = url.openConnection();
		uc.setConnectTimeout(5000);
		uc.setReadTimeout(5000);
		InputStream is = uc.getInputStream();
		// deflate, if necesarily
		if ("gzip".equals(uc.getContentEncoding())) {
			is = new GZIPInputStream(is);
		}
		return is;
	}

	// retrieves into an String object
	public String retrieve(String urlToRetrieve) throws MalformedURLException, IOException {
		InputStream is = prepareInputStream(urlToRetrieve);
		String encoding = "UTF-8";
		BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));
		StringBuilder output = new StringBuilder();
		String str;
		boolean first = true;
		while ((str = in.readLine()) != null) {
			if (!first)
				output.append("\n");
			first = false;
			output.append(str);
		}
		in.close();
		return output.toString();
	}

}
