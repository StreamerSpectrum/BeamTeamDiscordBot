package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team;

import java.util.ArrayList;
import java.util.List;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class TeamList extends Command {

	public TeamList() {
		this.name = "teamlist";
		this.help = "Displays a list of the teams this server is tracking.";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {

		List<BeamTeam> teams = new ArrayList<BeamTeam>(GuildManager.getGuild(event.getGuild()).getTrackedTeams());

		if (teams.size() > 0) {
			StringBuilder teamsSB = new StringBuilder();

			for (BeamTeam team : teams) {
				teamsSB.append(team.token).append("\n");
			}

			CommandHelper.sendPagination(event, teamsSB.toString().split("\n"), 1,
					"This Server is Tracking the Following Teams");
		} else {
			JDAManager.sendMessage(event, "This server is not tracking any teams.");
		}
	}

}
