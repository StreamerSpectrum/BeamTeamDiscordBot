package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.log;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class LogRemove extends Command {

	public LogRemove() {
		this.name = "logremove";
		this.help = "Removes the log channel from this server's settings.";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		BTBGuild guild = GuildManager.getGuild(event.getGuild());
		if (guild.getLogChannelID() != null) {
			guild.setLogChannelID(null);
		} else {
			JDAManager.sendMessage(event, "There is log channel set for this server.");
		}
	}

}
