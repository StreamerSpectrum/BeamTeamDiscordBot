package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.util.concurrent.ExecutionException;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
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
			if (GuildManager.getGuild(event.getGuild().getId()).addTeam(team)) {
				event.getChannel().sendMessage(String.format("%s has been added to the team tracker.", team.name))
						.queue();
			} else {
				event.getChannel()
						.sendMessage(String.format("%s is already in the list of tracked teams.", team.name))
						.queue();
			}
		}
	}

}
