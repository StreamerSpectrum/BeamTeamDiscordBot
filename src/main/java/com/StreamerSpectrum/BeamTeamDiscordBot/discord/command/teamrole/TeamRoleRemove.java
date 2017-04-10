package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.teamrole;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class TeamRoleRemove extends Command {

	public TeamRoleRemove() {
		this.name = "teamroleremove";
		this.help = "Takes in a team disassociates it from its configured role.";
		this.arguments = "teamNameOrID";
		this.userPermissions = new Permission[] { Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String arg = event.getArgs();

			BeamTeam team = CommandHelper.getTeam(event, arg);

			if (null != team) {
				if (DbManager.deleteTeamRole(Long.parseLong(event.getGuild().getId()), team.id)) {
					JDAManager.sendMessage(event, "Successfully removed %s's role!", team.name);
				} else {
					JDAManager.sendMessage(event,
							"I was unable to remove %s's role from your configuration. Please make sure that team has a role set.",
							team.name);
				}
			}
		} else {
			JDAManager.sendMessage(event, "Missing arguments from command!");
		}
	}

}
