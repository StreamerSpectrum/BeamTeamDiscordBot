package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel;

import java.util.ArrayList;
import java.util.List;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class ChannelList extends Command {

	public ChannelList() {
		this.name = "channellist";
		this.help = "Displays a list of the channels this server is tracking.";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {

		List<BTBBeamChannel> channels = new ArrayList<>(GuildManager.getGuild(event.getGuild()).getTrackedChannels());

		if (channels.size() > 0) {
			StringBuilder channelSB = new StringBuilder();

			for (BTBBeamChannel team : channels) {
				channelSB.append(team.token).append("\n");
			}

			CommandHelper.sendPagination(event, channelSB.toString().split("\n"), 1,
					"This Server is Tracking the Following Channels");
		} else {
			JDAManager.sendMessage(event, "This server is not tracking any channels.");
		}
	}
}
