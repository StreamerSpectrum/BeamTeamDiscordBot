package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.teamrole;

import java.sql.SQLException;
import java.util.List;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBRole;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class TeamRoleDistribute extends Command {

	public TeamRoleDistribute() {
		this.name = "teamroledistribute";
		this.help = "Distributes this server's team roles to their members.";
		this.userPermissions = new Permission[] { Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {
		try {
			List<BTBRole> roles = DbManager.readTeamRolesForGuild(Long.parseLong(event.getGuild().getId()));

			for (BTBRole role : roles) {
				JDAManager.distributeTeamRoleToGuildTeamMembers(role);
			}
			
			JDAManager.sendMessage(event, "Team role distribution complete!");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
