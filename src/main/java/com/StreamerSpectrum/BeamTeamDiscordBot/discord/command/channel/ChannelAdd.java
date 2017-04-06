package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class ChannelAdd extends Command {

	public ChannelAdd() {
		this.name = "channeladd";
		this.help = "Takes in a space-separated list of one or more usernames and adds them to this server's tracker.";
		this.arguments = "namesOrIDs...";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String args[] = event.getArgs().split(" ");
			BTBGuild guild = GuildManager.getGuild(event.getGuild());

			for (String channelArg : args) {
				BTBBeamChannel channel = CommandHelper.getChannel(event, channelArg);

				if (null != channel) {
					if (guild.addChannel(channel)) {
						JDAManager.sendMessage(event,
								String.format("%s's channel has been added to the tracker.", channel.user.username));
					} else {
						JDAManager.sendMessage(event, String.format(
								"%s's channel is already in the list of tracked channels.", channel.user.username));
					}
				}
			}
		} else {
			JDAManager.sendMessage(event, "Missing arguments from command!");
		}
	}

}
