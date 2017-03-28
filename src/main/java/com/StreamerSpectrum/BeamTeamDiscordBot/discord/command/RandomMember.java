package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

public class RandomMember extends Command {

	public RandomMember() {
		this.name = "randommember";
		this.help = "displays info about a random member from the specified team";
		this.arguments = "teamNameOrID";
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
			try {
				List<BeamTeamUser> teamMembers = BeamManager.getTeamMembers(team);
				BeamTeamUser member = teamMembers.get(new Random().nextInt(teamMembers.size()));

				event.getChannel().sendMessage(new EmbedBuilder()
						.setTitle(member.username, String.format("https://beam.pro/%s", member.username))
						.setThumbnail(String.format("https://beam.pro/api/v1/users/%d/avatar?w=64&h=64", member.id))
						.setDescription(StringUtils.isEmpty(member.bio) ? "No bio" : member.bio)
						.addField("Followers", Integer.toString(member.channel.numFollowers), true)
						.addField("Views", Integer.toString(member.channel.viewersTotal), true)
						.addField("Partnered", member.channel.partnered ? "Yes" : "No", true)
						.addField("Primary Team", BeamManager.getTeam(member.primaryTeam).name, true)
						.setFooter("Beam.pro",
								"https://github.com/WatchBeam/beam-branding-kit/blob/master/png/logo-ball.png?raw=true")
						.setTimestamp(Instant.now()).setColor(Color.CYAN).build()).queue();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				event.getChannel()
						.sendMessage(
								"Sorry, something went wrong. Please try again. If the issue continues, please contact a developer.")
						.queue();
			}
		}
	}

}
