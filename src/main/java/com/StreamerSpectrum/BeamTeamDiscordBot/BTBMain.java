package com.StreamerSpectrum.BeamTeamDiscordBot;

import javax.security.auth.login.LoginException;

import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class BTBMain {

	public static void main(String[] args) throws LoginException, RateLimitedException, InterruptedException {
		JDAManager.getJDA(); // TODO: Replace this with the ShardManager.

		load();
	}

	public static void load() {
		ConstellationManager.getConnectable();
	}

}
