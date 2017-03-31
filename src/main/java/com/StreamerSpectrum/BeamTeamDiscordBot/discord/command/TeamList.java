package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.util.ArrayList;
import java.util.List;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class TeamList extends Command {

	public TeamList() {
		this.name = "teamlist";
		this.help = "Displays a list of the teams this server is tracking.";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {

		List<BeamTeam> teams = new ArrayList<BeamTeam>(
				GuildManager.getGuild(event.getGuild().getId()).getTeams().values());

		if (teams.size() > 0) {
			StringBuilder teamsSB = new StringBuilder();

			for (BeamTeam team : teams) {
				teamsSB.append(team.token).append("\n");
			}

			CommandHelper.sendPagination(event, teamsSB.toString().split("\n"), 1,
					"This Server is Tracking the Following Teams");
		} else {
			CommandHelper.sendMessage(event, "This server is not tracking any teams.");
		}
	}

}
