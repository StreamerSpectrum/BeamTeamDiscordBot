package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.CommandEvent;
import me.jagrosh.jdautilities.menu.pagination.PaginatorBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.exceptions.PermissionException;

public abstract class CommandHelper {
	public static final Color	COLOR			= new Color(76, 144, 243);
	public static final String	BEAM_LOGO_URL	= "https://github.com/WatchBeam/beam-branding-kit/blob/master/png/logo-ball.png?raw=true";

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

	public static BTBBeamChannel getChannel(CommandEvent event, String nameOrID) {
		BTBBeamChannel channel = null;

		try {
			int id = Integer.parseInt(nameOrID);
			channel = BeamManager.getChannel(id);
		} catch (NumberFormatException e) {
			try {
				channel = BeamManager.getChannel(nameOrID);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				sendMessage(event, String.format("Sorry, I can't find the Beam channel named %s.", nameOrID));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			sendMessage(event, String.format("Sorry, I can't find the Beam channel with ID %s.", nameOrID));
		}

		return channel;
	}

	public static List<BeamTeamUser> getTeamMembers(CommandEvent event, BeamTeam team) {
		List<BeamTeamUser> members = null;

		members = BeamManager.getTeamMembers(team);

		return members;
	}

	public static PaginatorBuilder buildPagination(CommandEvent event, String[] listItems, int numCols, String title) {
		return new PaginatorBuilder().setText(title).setItems(listItems)
				.setColumns(numCols < 1 ? 1 : numCols > 3 ? 3 : numCols).setFinalAction(m -> {
					try {
						m.clearReactions().queue();
					} catch (PermissionException e) {}
				}).setItemsPerPage(10).waitOnSinglePage(false).useNumberedItems(true).showPageNumbers(true)
				.setEventWaiter(JDAManager.getWaiter()).setTimeout(1, TimeUnit.MINUTES).setUsers(event.getAuthor())
				.setColor(CommandHelper.COLOR);
	}

	public static PaginatorBuilder buildPagination(CommandEvent event, String[] listItems, int numCols, String format,
			Object... args) {
		return buildPagination(event, listItems, numCols, String.format(format, args));
	}

	public static void sendPagination(CommandEvent event, String[] listItems, int numCols, String title) {
		sendPagination(event, buildPagination(event, listItems, numCols, title));
	}

	public static void sendPagination(CommandEvent event, String[] listItems, int numCols, String format,
			Object... args) {
		sendPagination(event, buildPagination(event, listItems, numCols, format, args));
	}

	public static void sendPagination(CommandEvent event, PaginatorBuilder builder) {
		builder.build().paginate(event.getChannel(), 0);
	}

	public static void sendTeamUserEmbed(CommandEvent event, BeamTeamUser member) {
		try {
			sendMessage(event,
					new EmbedBuilder().setTitle(member.username, String.format("https://beam.pro/%s", member.username))
							.setThumbnail(String.format("https://beam.pro/api/v1/users/%d/avatar?_=%d", member.id, new Random().nextInt()))
							.setDescription(StringUtils.isBlank(member.bio) ? "No bio" : member.bio)
							.addField("Followers", Integer.toString(member.channel.numFollowers), true)
							.addField("Views", Integer.toString(member.channel.viewersTotal), true)
							.addField("Partnered", member.channel.partnered ? "Yes" : "No", true)
							.addField("Primary Team", BeamManager.getTeam(member.primaryTeam).name, true)
							.addField("Joined Beam", member.createdAt.toString(), true)
							.addField("Member Since", member.teamMembership.createdAt.toString(), true)
							.setImage(String.format("https://thumbs.beam.pro/channel/%d.small.jpg?_=%d",
									member.channel.id, new Random().nextInt()))
							.setFooter("Beam.pro", BEAM_LOGO_URL).setTimestamp(Instant.now()).setColor(COLOR).build());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			sendMessage(event,
					"Sorry, something went wrong. Please try again. If the issue continues, please contact a developer.");
		}
	}

	public static String sendMessage(CommandEvent event, Message msg) {
		event.getChannel().sendMessage(msg).queue();
		return event.getChannel().getLatestMessageId();
	}

	public static String sendMessage(CommandEvent event, MessageEmbed embed) {
		event.getChannel().sendMessage(embed).queue();
		return event.getChannel().getLatestMessageId();
	}

	public static String sendMessage(CommandEvent event, String text) {
		event.getChannel().sendMessage(text).queue();
		return event.getChannel().getLatestMessageId();
	}

	public static String sendMessage(CommandEvent event, String format, Object... args) {
		event.getChannel().sendMessage(String.format(format, args)).queue();
		return event.getChannel().getLatestMessageId();
	}
}
