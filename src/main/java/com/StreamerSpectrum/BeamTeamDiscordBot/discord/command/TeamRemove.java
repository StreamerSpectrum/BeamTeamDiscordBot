package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class TeamRemove extends Command {

	public TeamRemove() {
		this.name = "teamremove";
		this.help = "removes a Beam team from the tracker";
		this.arguments = "nameOrID";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			BeamTeam team = CommandHelper.getTeam(event.getArgs(), event);

			if (null != team) {
				if (GuildManager.getGuild(event.getGuild().getId()).removeTeam(team)) {
					event.getChannel()
							.sendMessage(String.format("%s has been removed from the team tracker.", team.name))
							.queue();
				} else {
					event.getChannel()
							.sendMessage(String.format("%s was not found in the list of tracked teams.", team.name))
							.queue();
				}
			}
		} else {
			event.getChannel().sendMessage("Missing arguments from command!").queue();
		}
	}
}
