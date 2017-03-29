package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;

public class PrimaryTeam extends Command {

	public PrimaryTeam() {
		this.name = "primaryteam";
		this.help = "returns a list of users from [teamNameOrID] whose primary team matches/doesn't match [isPrimary] the team";
		this.arguments = "teamNameOrID isPrimary";
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String args[] = event.getArgs().split(" ");

			if (args.length > 0) {
				BeamTeam team = CommandHelper.getTeam(args[0], event);

				if (team != null) {
					boolean isPrimary = args.length == 1 || (args.length == 2 && Boolean.parseBoolean(args[1]));
					int total = 0;

					try {
						List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

						StringBuilder sb = new StringBuilder();

						if (isPrimary) {
							sb.append(String.format("Members with %s as their primary team: ", team.name));

							for (BeamTeamUser member : members) {
								if (member.primaryTeam != null && member.primaryTeam.intValue() == team.id.intValue()) {
									sb.append(String.format("%s, ", member.username));
									++total;
								}
							}
						} else {
							sb.append(String.format("Members without %s as their primary team: ", team.name));

							for (BeamTeamUser member : members) {
								if (member.primaryTeam == null || member.primaryTeam.intValue() != team.id.intValue()) {
									sb.append(String.format("%s, ", member.username));
									++total;
								}
							}
						}
						
						if (sb.toString().contains(",")) {
							event.getChannel().sendMessage(sb.substring(0, sb.lastIndexOf(","))).queue();
						} else {
							event.getChannel().sendMessage(sb.append("None").toString()).queue();
						}

						event.getChannel().sendMessage(String.format("Total: %d", total)).queue();

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						event.getChannel()
								.sendMessage(String.format("Unable to retrieve team members for %s.", team.name))
								.queue();
					}
				}
			}
		} else {
			event.getChannel().sendMessage("Missing arguments from command!").queue();
		}
	}

}
