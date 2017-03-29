package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;

import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

public abstract class CommandHelper {
	public static final Color COLOR = new Color(76, 144, 243);
	public static final String BEAM_LOGO_URL = "https://github.com/WatchBeam/beam-branding-kit/blob/master/png/logo-ball.png?raw=true";

	public static BeamTeam getTeam(CommandEvent event, String teamNameOrID) {
		BeamTeam team = null;

		try {
			int id = Integer.parseInt(teamNameOrID);
			team = BeamManager.getTeam(id);
		} catch (NumberFormatException e) {
			try {
				team = BeamManager.getTeam(teamNameOrID);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				sendMessage(event, String.format("Sorry, I can't find the Beam team named %s.", teamNameOrID));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			sendMessage(event, String.format("Sorry, I can't find the Beam team with ID %s.", teamNameOrID));
		}

		return team;
	}

	public static List<BeamTeamUser> getTeamMembers(CommandEvent event, BeamTeam team) {
		List<BeamTeamUser> members = null;

		try {
			members = BeamManager.getTeamMembers(team);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			CommandHelper.sendMessage(event, String.format("Unable to retrieve team members for %s.", team.name));
		}

		return members;
	}

	public static void sendUserEmbed(CommandEvent event, BeamTeamUser member) {
		try {
			sendMessage(event,
					new EmbedBuilder().setTitle(member.username, String.format("https://beam.pro/%s", member.username))
							.setThumbnail(String.format("https://beam.pro/api/v1/users/%d/avatar?w=64&h=64", member.id))
							.setDescription(StringUtils.isEmpty(member.bio) ? "No bio" : member.bio)
							.addField("Followers", Integer.toString(member.channel.numFollowers), true)
							.addField("Views", Integer.toString(member.channel.viewersTotal), true)
							.addField("Partnered", member.channel.partnered ? "Yes" : "No", true)
							.addField("Primary Team", BeamManager.getTeam(member.primaryTeam).name, true)
							.setImage(String.format("https://thumbs.beam.pro/channel/%d.small.jpg", member.channel.id))
							.setFooter("Beam.pro", BEAM_LOGO_URL).setTimestamp(Instant.now()).setColor(COLOR).build());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			sendMessage(event,
					"Sorry, something went wrong. Please try again. If the issue continues, please contact a developer.");
		}
	}

	public static void sendMessage(CommandEvent event, Message msg) {
		event.getChannel().sendMessage(msg).queue();
	}

	public static void sendMessage(CommandEvent event, MessageEmbed embed) {
		event.getChannel().sendMessage(embed).queue();
	}

	public static void sendMessage(CommandEvent event, String text) {
		event.getChannel().sendMessage(text).queue();
	}

	public static void sendMessage(CommandEvent event, String format, Object... args) {
		event.getChannel().sendMessage(String.format(format, args)).queue();
	}
}
