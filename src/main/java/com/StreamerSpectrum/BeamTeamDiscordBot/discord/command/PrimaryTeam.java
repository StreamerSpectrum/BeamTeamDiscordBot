package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

public class PrimaryTeam extends Command {

	public PrimaryTeam() {
		this.name = "primaryteam";
		this.help = "returns an embedded message that details who on the team have it set as their primary, and who don't";
		this.arguments = "teamNameOrID";
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String args = event.getArgs();
			BeamTeam team = CommandHelper.getTeam(event, args);

			if (null != team) {
				int primaryTotal = 0, nonPrimaryTotal = 0;
				StringBuilder primaryMembers = new StringBuilder();
				StringBuilder nonPrimaryMembers = new StringBuilder();

				List<BeamTeamUser> members = CommandHelper.getTeamMembers(event, team);

				for (BeamTeamUser member : members) {
					if (member.primaryTeam != null && member.primaryTeam.intValue() == team.id.intValue()) {
						primaryMembers.append(String.format("%s\n", member.username));
						++primaryTotal;
					} else {
						nonPrimaryMembers.append(String.format("%s\n", member.username));
						++nonPrimaryTotal;
					}
				}

				if (StringUtils.isBlank(primaryMembers.toString())) {
					primaryMembers.append("None");
				}

				if (StringUtils.isBlank(nonPrimaryMembers.toString())) {
					primaryMembers.append("None");
				}

				CommandHelper.sendMessage(event,
						new EmbedBuilder()
								.setTitle(String.format("Primary Team results for %s", team.name),
										String.format("https://beam.pro/team/%s", team.token))
								.setThumbnail(team.logoUrl)
								.addField(String.format("Primary Team Match | %d", primaryTotal),
										primaryMembers.toString(), true)
								.addField(String.format("Primary Team Other | %d", nonPrimaryTotal),
										nonPrimaryMembers.toString(), true)
								.addBlankField(true).setFooter("Beam.pro", CommandHelper.BEAM_LOGO_URL)
								.setTimestamp(Instant.now()).setColor(CommandHelper.COLOR).build());
			}
		} else {
			CommandHelper.sendMessage(event, "Missing arguments from command!");
		}
	}

}
