package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class ChannelRemove extends Command {

	public ChannelRemove() {
		this.name = "channelremove";
		this.help = "Takes in a space-separated list of one or more channels and removes them from this server's tracker.";
		this.arguments = "namesOrIDs...";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String args[] = event.getArgs().split(" ");

			for (String channelArg : args) {
				BTBBeamChannel channel = CommandHelper.getChannel(event, channelArg);

				if (null != channel) {
					if (GuildManager.getGuild(event.getGuild()).removeChannel(channel)) {
						JDAManager.sendMessage(event, String.format(
								"%s's channel has been removed from the channel tracker.", channel.user.username));
					} else {
						JDAManager.sendMessage(event, String.format(
								"%s's channel was not found in the list of tracked channels.", channel.user.username));
					}
				}
			}
		} else {
			JDAManager.sendMessage(event, "Missing arguments from command!");
		}
	}

}
