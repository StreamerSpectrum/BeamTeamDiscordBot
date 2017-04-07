package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;

public class RestartConstellation extends Command {

	public RestartConstellation() {
		this.name = "restartconstellation";
		this.help = "Restarts Beam constellation connection used for announcements";
	}

	@Override
	protected void execute(CommandEvent event) {
		ConstellationManager.restartConstellation();
	}

}
