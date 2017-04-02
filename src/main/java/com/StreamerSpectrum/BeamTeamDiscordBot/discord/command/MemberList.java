package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;

public class MemberList extends Command {

	public MemberList() {
		this.name = "memberlist";
		this.help = "Takes in a list of teams and displays a paginated list of each of their members.";
		this.arguments = "teamNamesOrIDs...";
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			memberListHelper(event, event.getArgs().split(" "));
		} else if (!GuildManager.getGuild(event.getGuild().getId()).getTracker().getTeams().isEmpty()) {
			List<String> args = new ArrayList<String>();
			List<BeamTeam> trackedTeams = new ArrayList<BeamTeam>(
					GuildManager.getGuild(event.getGuild().getId()).getTracker().getTeams().values());

			for (BeamTeam team : trackedTeams) {
				args.add(team.token);
			}

			memberListHelper(event, args.toArray(new String[] {}));
		} else {
			CommandHelper.sendMessage(event, "Missing arguments from command!");
		}
	}

	private void memberListHelper(CommandEvent event, String[] args) {
		for (String teamArg : args) {
			BeamTeam team = CommandHelper.getTeam(event, teamArg);

			if (null != team) {
				StringBuilder teamMembersSB = new StringBuilder();

				List<BeamTeamUser> members = CommandHelper.getTeamMembers(event, team);

				for (BeamTeamUser member : members) {
					teamMembersSB
							.append(String.format("[%s](https://beam.pro/%s)\n", member.username, member.username));
				}

				CommandHelper.sendPagination(event, teamMembersSB.toString().split("\n"), 1, "%s Has %d Members",
						team.name, members.size());
			}
		}
	}

}
