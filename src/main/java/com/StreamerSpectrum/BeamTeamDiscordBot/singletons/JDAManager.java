package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.security.auth.login.LoginException;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.RandomMember;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive.GoLiveSet;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.stream.StreamAdd;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.stream.StreamRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamAdd;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamList;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.MemberInfo;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.MemberList;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.FollowReport;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.PrimaryTeam;

import me.jagrosh.jdautilities.commandclient.CommandClient;
import me.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

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
							new MemberInfo(), new FollowReport(), new MemberList(), new GoLiveSet(), new StreamAdd(), new StreamRemove())
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
			getJDA().getTextChannelById(channelID).sendMessage(msg).queue();
			return getJDA().getTextChannelById(channelID).getLatestMessageId();
		} catch (IllegalArgumentException | RateLimitedException e) {
			return null;
		}
	}

	public static String sendMessage(String channelID, MessageEmbed embed) {
		try {
			getJDA().getTextChannelById(channelID).sendMessage(embed).queue();
			return getJDA().getTextChannelById(channelID).getLatestMessageId();
		} catch (IllegalArgumentException | RateLimitedException e) {
			return null;
		}
	}

	public static String sendMessage(String channelID, String text) {
		try {
			getJDA().getTextChannelById(channelID).sendMessage(text).queue();
			return getJDA().getTextChannelById(channelID).getLatestMessageId();
		} catch (IllegalArgumentException | RateLimitedException e) {
			return null;
		}
	}

	public static String sendMessage(String channelID, String format, Object... args) {
		try {
			getJDA().getTextChannelById(channelID).sendMessage(format, args).queue();
			return getJDA().getTextChannelById(channelID).getLatestMessageId();
		} catch (IllegalArgumentException | RateLimitedException e) {
			return null;
		}
	}
}
