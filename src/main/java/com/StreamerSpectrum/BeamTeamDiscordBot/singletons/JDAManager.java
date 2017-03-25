package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.security.auth.login.LoginException;

import me.jagrosh.jdautilities.commandclient.CommandClient;
import me.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public abstract class JDAManager {

	private static JDA jda;
	private static CommandClient commandClient;
	private static EventWaiter waiter;

	public static JDA getJDA() {
		if (null == jda) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("/config.txt")))) {
				String botToken = br.readLine();

				jda = new JDABuilder(AccountType.BOT)
						.setToken(botToken)
						.setStatus(OnlineStatus.DO_NOT_DISTURB)
						.setGame(Game.of("loading..."))		
						.addListener(waiter)
						.addListener(getCommandClient())
						.buildAsync();
			} catch (LoginException | IllegalArgumentException | RateLimitedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return jda;
	}

	public static CommandClient getCommandClient() {
		if (null == commandClient) {
			commandClient = new CommandClientBuilder()
					.useDefaultGame()
					.setPrefix("!")
					.addCommands(/*add commands here*/)
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
}
