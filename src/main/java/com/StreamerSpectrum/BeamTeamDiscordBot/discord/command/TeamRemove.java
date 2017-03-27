package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.util.concurrent.ExecutionException;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
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
		String args = event.getArgs();
		BeamTeam team = null;

		try {
			int id = Integer.parseInt(args);
			team = BeamManager.getTeam(id);
		} catch (NumberFormatException e) {
			try {
				team = BeamManager.getTeam(args);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				event.getChannel().sendMessage(String.format("Sorry, I can't find the Beam team named %s.", args))
						.queue();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			event.getChannel().sendMessage(String.format("Sorry, I can't find the Beam team with ID %s.", args))
					.queue();
		}

		if (null != team) {
			if (GuildManager.getGuild(event.getGuild().getId()).removeTeam(team)) {
				event.getChannel().sendMessage(String.format("%s has been removed from the team tracker.", team.name))
						.queue();
			} else {
				event.getChannel()
						.sendMessage(String.format("%s was not found in the list of tracked teams.", team.name))
						.queue();
			}
		}
	}
}
