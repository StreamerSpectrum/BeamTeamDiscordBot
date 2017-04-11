package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class GoLiveRemove extends Command {

	public GoLiveRemove() {
		this.name = "goliveremove";
		this.help = "Removes the go-live channel from this server's settings.";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		BTBGuild guild = GuildManager.getGuild(event.getGuild());
		if (guild.getGoLiveChannelID() != null) {
			guild.setGoLiveChannelID(null);
			JDAManager.sendMessage(event, "Go-live channel has been removed for this server.");
		} else {
			JDAManager.sendMessage(event, "There is no go-live channel set for this server.");
		}
	}

}
