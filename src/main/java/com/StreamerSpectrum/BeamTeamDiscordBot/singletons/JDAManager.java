package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.RandomMember;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.admin.RestartConstellation;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel.ChannelRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel.ChannelAdd;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel.ChannelList;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive.GoLiveDeleteOffline;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive.GoLiveRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive.GoLiveSet;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamAdd;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamList;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.GoLiveMessage;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.MemberInfo;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.MemberList;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.FollowReport;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.PrimaryTeam;

import me.jagrosh.jdautilities.commandclient.CommandClient;
import me.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import pro.beam.api.util.Enums;

public abstract class JDAManager {

	private static JDA				jda;
	private static CommandClient	commandClient;
	private static EventWaiter		waiter;

	public static JDA getJDA() throws IllegalArgumentException, RateLimitedException {
		if (null == jda) {
			try (BufferedReader br = new BufferedReader(new FileReader(new File("resources/config.txt")))) {
				String botToken = br.readLine();

				jda = new JDABuilder(AccountType.BOT).setToken(botToken).setStatus(OnlineStatus.DO_NOT_DISTURB)
						.setGame(Game.of("loading...")).addListener(getWaiter()).addListener(getCommandClient())
						.buildAsync();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return jda;
	}

	private static CommandClient getCommandClient() {
		if (null == commandClient) {
			commandClient = new CommandClientBuilder().useDefaultGame().setPrefix("!btb ")
					.addCommands(new TeamAdd(), new TeamRemove(), new TeamList(), new RandomMember(), new PrimaryTeam(),
							new MemberInfo(), new FollowReport(), new MemberList(), new GoLiveSet(), new GoLiveRemove(),
							new GoLiveDeleteOffline(), new ChannelAdd(), new ChannelRemove(), new ChannelList(),
							new RestartConstellation())
					.build();
		}

		return commandClient;
	}

	public static EventWaiter getWaiter() {
		if (null == waiter) {
			waiter = new EventWaiter();
		}

		return waiter;
	}

	public static String sendMessage(String channelID, Message msg) {
		try {
			return getJDA().getTextChannelById(channelID).sendMessage(msg).complete().getId();
		} catch (IllegalArgumentException | RateLimitedException e) {
			return null;
		}
	}

	public static String sendMessage(String channelID, MessageEmbed embed) {
		try {
			return getJDA().getTextChannelById(channelID).sendMessage(embed).complete().getId();
		} catch (IllegalArgumentException | RateLimitedException e) {
			return null;
		}
	}

	public static String sendMessage(String channelID, String text) {
		try {
			return getJDA().getTextChannelById(channelID).sendMessage(text).complete().getId();
		} catch (IllegalArgumentException | RateLimitedException e) {
			return null;
		}
	}

	public static String sendMessage(String channelID, String format, Object... args) {
		try {
			return getJDA().getTextChannelById(channelID).sendMessage(format, args).complete().getId();
		} catch (IllegalArgumentException | RateLimitedException e) {
			return null;
		}
	}

	public static String sendMessage(CommandEvent event, Message msg) {
		return sendMessage(event.getChannel().getId(), msg);
	}

	public static String sendMessage(CommandEvent event, MessageEmbed embed) {
		return sendMessage(event.getChannel().getId(), embed);
	}

	public static String sendMessage(CommandEvent event, String text) {
		return sendMessage(event.getChannel().getId(), text);
	}

	public static String sendMessage(CommandEvent event, String format, Object... args) {
		return sendMessage(event.getChannel().getId(), format, args);
	}

	public static void deleteMessage(GoLiveMessage message) {
		deleteMessage(message.getMessageID(), message.getGoLiveChannelID());
	}

	public static void deleteMessage(String messageID, String goLiveChannelID) {
		try {
			getJDA().getTextChannelById(goLiveChannelID).deleteMessageById(messageID).queue();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RateLimitedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static MessageEmbed buildGoLiveEmbed(BTBBeamChannel channel) {
		return new EmbedBuilder()
				.setTitle(String.format("%s is now live!", channel.user.username),
						String.format("https://beam.pro/%s", channel.user.username))
				.setThumbnail(String.format("https://beam.pro/api/v1/users/%d/avatar?_=%d", channel.user.id,
						new Random().nextInt()))
				.setDescription(StringUtils.isBlank(channel.user.bio) ? "No bio" : channel.user.bio)
				.addField(channel.name, channel.type == null ? "No game selected" : channel.type.name, false)
				.addField("Followers", Integer.toString(channel.numFollowers), true)
				.addField("Views", Integer.toString(channel.viewersTotal), true)
				.addField("Rating", Enums.serializedName(channel.audience), true)
				.setImage(String.format("https://thumbs.beam.pro/channel/%d.small.jpg?_=%d", channel.id,
						new Random().nextInt()))
				.setFooter("Beam.pro", CommandHelper.BEAM_LOGO_URL).setTimestamp(Instant.now())
				.setColor(CommandHelper.COLOR).build();
	}

	public static PrivateChannel getPrivateChannel(User user) {
		if (!user.hasPrivateChannel()) {
			user.openPrivateChannel();
		}

		return user.getPrivateChannel();
	}

	public static void sendDM(CommandEvent event, Message msg) {
		getPrivateChannel(event.getAuthor()).sendMessage(msg);
	}

	public static void sendDM(CommandEvent event, MessageEmbed embed) {
		getPrivateChannel(event.getAuthor()).sendMessage(embed);
	}

	public static void sendDM(CommandEvent event, String text) {
		getPrivateChannel(event.getAuthor()).sendMessage(text);
	}

	public static void sendDM(CommandEvent event, String format, Object... args) {
		getPrivateChannel(event.getAuthor()).sendMessage(format, args);
	}

}
