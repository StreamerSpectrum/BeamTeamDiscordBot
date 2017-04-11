package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class TeamRemove extends Command {

	public TeamRemove() {
		this.name = "teamremove";
		this.help = "Takes in a space-separated list of one or more teams and removes them from this server's tracker.";
		this.arguments = "namesOrIDs...";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String args[] = event.getArgs().split(" ");

			for (String teamArg : args) {
				BeamTeam team = CommandHelper.getTeam(event, teamArg);

				if (null != team) {
					if (GuildManager.getGuild(event.getGuild()).removeTeam(team)) {
						JDAManager.sendMessage(event,
								String.format("%s has been removed from the team tracker.", team.name));
					} else {
						JDAManager.sendMessage(event,
								String.format("%s was not found in the list of tracked teams.", team.name));
					}
					
					if (DbManager.readGuildsForTrackedTeam(team.id, false, false, false).isEmpty()) {
						ConstellationManager.unsubscribeFromTeam(team);
					}
				}
			}
		} else {
			JDAManager.sendMessage(event, "Missing arguments from command!");
		}
	}
}
