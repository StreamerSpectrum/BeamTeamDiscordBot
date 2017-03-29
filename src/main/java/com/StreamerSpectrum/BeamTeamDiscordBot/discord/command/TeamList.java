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
		this.help = "lists the Beam teams this server is tracking";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {

		List<BeamTeam> teams = new ArrayList<BeamTeam>(
				GuildManager.getGuild(event.getGuild().getId()).getTeams().values());

		if (teams.size() > 0) {
			StringBuilder sb = new StringBuilder("This server is tracking the following teams: ");

			for (BeamTeam team : teams) {
				sb.append(team.token).append(", ");
			}

			CommandHelper.sendMessage(event, sb.toString().substring(0, sb.lastIndexOf(",")));
		} else {
			CommandHelper.sendMessage(event, "This server is not tracking any teams.");
		}
	}

}
