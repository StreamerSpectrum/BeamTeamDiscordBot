package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;

public class MemberInfo extends Command {

	public MemberInfo() {
		this.name = "memberinfo";
		this.help = "Takes in a team name and a space-separated list of users and displays their info.";
		this.arguments = "teamNameOrID usernamesOrIDs...";
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs()) && event.getArgs().split(" ").length >= 2) {
			String teamArg = event.getArgs().split(" ")[0];
			List<String> userArgs = new ArrayList<String>(Arrays.asList(event.getArgs().split(" ")));
			userArgs.remove(0);

			BeamTeam team = CommandHelper.getTeam(event, teamArg);

			if (null != team) {
				try {
					List<BeamTeamUser> teamMembers = BeamManager.getTeamMembers(team);

					for (String userArg : userArgs) {
						BeamTeamUser foundMember = null;

						for (int retry = 0; retry < 5 && foundMember == null; ++retry) {
							for (BeamTeamUser member : teamMembers) {
								try {
									if (member.id.intValue() == Integer.parseInt(userArg)) {
										foundMember = member;
										break;
									}
								} catch (NumberFormatException e1) {
									if (StringUtils.equalsIgnoreCase(member.username, userArg)) {
										foundMember = member;
										break;
									}
								}
							}

							if (null != foundMember) {
								CommandHelper.sendTeamUserEmbed(event, foundMember);
							} else if (retry >= 4) {
								CommandHelper.sendMessage(event, "Unable to find information for user %s.", userArg);
							} else {
								TimeUnit.SECONDS.sleep(5);
							}
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					CommandHelper.sendMessage(event, "Cannot find team members for %s!", team.name);
				}
			}
		} else {
			CommandHelper.sendMessage(event, "Missing arguments from command!");
		}
	}
}
