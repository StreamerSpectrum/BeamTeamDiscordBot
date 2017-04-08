package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;

public class RestartConstellation extends Command {

	public RestartConstellation() {
		this.name = "restartconstellation";
		this.help = "Restarts Beam constellation connection used for announcements";
	}

	@Override
	protected void execute(CommandEvent event) {
		if ("180390479437889536".equals(event.getAuthor().getId())) {
			ConstellationManager.restartConstellation();
			JDAManager.sendMessage(event,
					String.format("%s has restarted constellation.", event.getAuthor().getName()));
		} else {
			JDAManager.sendMessage(event, "You do not have permission to do that.");
		}
	}

}
