package com.StreamerSpectrum.BeamTeamDiscordBot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.security.auth.login.LoginException;

import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class BTBMain {
	private final static Logger	logger	= Logger.getLogger(BTBMain.class.getName());
	private static FileHandler	fh		= null;

	public static void main(String[] args) throws LoginException, RateLimitedException, InterruptedException {
		initLogger();

		JDAManager.getJDA(); // TODO: Replace this with the ShardManager.

		load();
	}

	private static void initLogger() {
		if (Files.notExists(Paths.get("logs\\"))) {
			new File("logs\\").mkdir();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");

		try {
			fh = new FileHandler("logs\\BTB-Logger_" + sdf.format(new Date()) + ".log", false);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		Logger l = Logger.getLogger("");
		fh.setFormatter(new SimpleFormatter());
		l.addHandler(fh);
		l.setLevel(Level.CONFIG);

		logger.log(Level.INFO, "Logger initialized");
	}

	public static void load() {
		ConstellationManager.getConnectable();
	}

}
