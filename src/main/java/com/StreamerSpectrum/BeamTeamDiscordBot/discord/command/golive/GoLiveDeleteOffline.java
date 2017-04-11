package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class GoLiveDeleteOffline extends Command {

	public GoLiveDeleteOffline() {
		this.name = "golivedeleteoffline";
		this.help = "Sets whether to delete go-live announcements when the streamer goes offline.";
		this.arguments = "true/false";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String arg = event.getArgs().trim();

			BTBGuild guild = GuildManager.getGuild(event.getGuild());

			guild.setRemoveOfflineChannelAnnouncements(Boolean.parseBoolean(arg));

			JDAManager.sendMessage(event, "Offline go-live message removal set to %b", Boolean.parseBoolean(arg));
		} else {
			JDAManager.sendMessage(event, "Missing arguments from command!");
		}
	}
}
