package com.StreamerSpectrum.BeamTeamDiscordBot;

import javax.security.auth.login.LoginException;

import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import net.dv8tion.jda.core.exceptions.RateLimitedException;

//TODO: Fix this classes name, It bugs me ~Nightmare
public class BeamTeamDiscordBotMain {

	public static void main(String[] args) throws LoginException, RateLimitedException, InterruptedException {
		JDAManager.getJDA(); //TODO: Replace this with the ShardManager.
	}

}
