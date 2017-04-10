package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.teamrole;

import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBRole;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

public class TeamRoleSet extends Command {

	public TeamRoleSet() {
		this.name = "teamroleset";
		this.help = "Takes in a team and a role name and automatically sets all Beam team members on this server to that role.";
		this.arguments = "teamNameOrID roleName";
		this.userPermissions = new Permission[] { Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String args[] = event.getArgs().split(" ");
			String teamArg = args[0];
			String roleArg = "";

			for (int i = 1; i < args.length; ++i) {
				roleArg += args[i] + " ";
			}

			roleArg = roleArg.trim();

			BTBGuild guild = GuildManager.getGuild(event.getGuild());

			BeamTeam team = CommandHelper.getTeam(event, teamArg);
			if (null != team) {
				Role role = event.getGuild().getRolesByName(roleArg, true).isEmpty() ? null
						: event.getGuild().getRolesByName(roleArg, true).get(0);

				if (null != team && null != role) {
					try {
						BTBRole teamRole = new BTBRole(guild.getID(), team.id, role.getId());

						if (DbManager.createTeamRole(teamRole)) {
							JDAManager.distributeTeamRoleToGuildTeamMembers(teamRole);

							JDAManager.sendMessage(event, "The role '%s' will now be added to all new %s members.",
									role.getName(), team.name);
						} else {
							JDAManager.sendMessage(event, "I am unable to associate the '%s' role with %s.",
									role.getName(), team.name);
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					JDAManager.sendMessage(event,
							String.format("I cannot find a role named '%s' on this server.", roleArg));
				}
			}
		} else {
			JDAManager.sendMessage(event, "Missing arguments from command!");
		}
	}

}
