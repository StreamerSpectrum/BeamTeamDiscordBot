package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;

public class FollowReport extends Command {

	public FollowReport() {
		this.name = "followreport";
		this.help = "Takes in a team name and a space-separated list of users and displays which members each user isn't following.";
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
						StringBuilder followingList = new StringBuilder();
						StringBuilder notFollowingList = new StringBuilder();
						int followingCount = 0, notFollowingCount = 0;

						BTBBeamUser user = BeamManager.getUser(userArg);

						List<BTBBeamChannel> userFollows = BeamManager.getFollowing(user);

						for (BeamTeamUser member : teamMembers) {
							if (!member.equals(user)) {
								boolean isNotFollowingMember = true;

								for (BTBBeamChannel followee : userFollows) {
									if (member.channel.id == followee.id) {
										followingList.append(String.format("[%s](https://beam.pro/%s)\n",
												member.username, member.username));
										isNotFollowingMember = false;
										++followingCount;
										break;
									} else if (member.channel.id < followee.id) {
										break;
									}
								}

								if (isNotFollowingMember) {
									notFollowingList.append(String.format("[%s](https://beam.pro/%s)\n",
											member.username, member.username));
									++notFollowingCount;
								}
							}
						}

						if (followingCount == 0) {
							followingList.append("NONE");
						}

						if (notFollowingCount == 0) {
							notFollowingList.append("NONE");
						}

						CommandHelper.sendPagination(event, followingList.toString().split("\n"), 1,
								"%s is Following %d/%d %s Members", user.username, followingCount, teamMembers.size(),
								team.name);

						CommandHelper.sendPagination(event, notFollowingList.toString().split("\n"), 1,
								"%s is Not Following %d/%d %s Members", user.username, notFollowingCount,
								teamMembers.size(), team.name);
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
