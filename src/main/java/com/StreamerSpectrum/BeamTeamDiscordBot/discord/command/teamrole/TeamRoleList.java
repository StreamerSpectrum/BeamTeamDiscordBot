package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.teamrole;

import java.sql.SQLException;
import java.util.List;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBRole;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;
import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class TeamRoleList extends Command {

	public TeamRoleList() {
		this.name = "teamrolelist";
		this.help = "Displays this server's list of team roles and their teams.";
		this.userPermissions = new Permission[] { Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {
		try {
			List<BTBRole> roles = DbManager.readTeamRolesForGuild(Long.parseLong(event.getGuild().getId()));

			StringBuilder sb = new StringBuilder();

			for (BTBRole role : roles) {
				sb.append(
						String.format("%s - %s", CommandHelper.getTeam(event, Integer.toString(role.getTeamID())).name,
								event.getGuild().getRoleById(role.getRoleID()).getName()));
			}

			if (StringUtils.isBlank(sb.toString())) {
				sb.append("NONE");
			}

			CommandHelper.sendPagination(event, sb.toString().split("\n"), 1,
					String.format("Team roles for %s.", event.getGuild().getName()));

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
