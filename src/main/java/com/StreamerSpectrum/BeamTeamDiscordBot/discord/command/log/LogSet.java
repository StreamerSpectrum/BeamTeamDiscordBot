package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.log;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

public class LogSet extends Command {

	public LogSet() {
		this.name = "logset";
		this.help = "Sets the target channel as the log channel for all of this server's activity to be displayed.";
		this.arguments = "channel";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String arg = event.getArgs().trim();

			try {
				List<TextChannel> textChannels = event.getJDA().getTextChannelsByName(arg, true);

				if (!textChannels.isEmpty()) {
					BTBGuild guild = GuildManager.getGuild(event.getGuild());

					guild.setLogChannelID(textChannels.get(0).getId());

					JDAManager.sendMessage(event, "Log channel has been set to %s.", arg);
				} else {
					JDAManager.sendMessage(event,
							"I can't find the channel named %s. Please ensure it exists and that I have read & write priveleges for it.",
							arg);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else {
			JDAManager.sendMessage(event, "Missing arguments from command!");
		}
	}

}
