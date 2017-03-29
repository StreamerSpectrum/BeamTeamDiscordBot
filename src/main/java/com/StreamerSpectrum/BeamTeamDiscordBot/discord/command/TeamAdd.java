package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class TeamAdd extends Command {

	public TeamAdd() {
		this.name = "teamadd";
		this.help = "adds a Beam team to the tracker";
		this.arguments = "nameOrID";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			BeamTeam team = CommandHelper.getTeam(event, event.getArgs());

			if (null != team) {
				if (GuildManager.getGuild(event.getGuild().getId()).addTeam(team)) {
					CommandHelper.sendMessage(event,
							String.format("%s has been added to the team tracker.", team.name));
				} else {
					CommandHelper.sendMessage(event,
							String.format("%s is already in the list of tracked teams.", team.name));
				}
			}
		} else {
			CommandHelper.sendMessage(event, "Missing arguments from command!");
		}
	}

}
