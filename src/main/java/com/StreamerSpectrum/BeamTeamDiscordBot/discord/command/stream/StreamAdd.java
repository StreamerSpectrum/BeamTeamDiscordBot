package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.stream;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.Guild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;
import pro.beam.api.resource.channel.BeamChannel;

public class StreamAdd extends Command {

	public StreamAdd() {
		this.name = "streamadd";
		this.help = "Takes in a space-separated list of one or more usernames and adds them to this server's tracker.";
		this.arguments = "namesOrIDs...";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String args[] = event.getArgs().split(" ");
			Guild guild = GuildManager.getGuild(event.getGuild().getId());

			for (String channelArg : args) {
				BeamChannel channel = CommandHelper.getChannel(event, channelArg);

				if (null != channel) {
					if (guild.getTracker().addChannel(channel)) {
						CommandHelper.sendMessage(event,
								String.format("%s's channel has been added to the tracker.", channel.user.username));
					} else {
						CommandHelper.sendMessage(event,
								String.format("%s's channel is already in the list of tracked channels.", channel.user.username));
					}
				}
			}
		} else {
			CommandHelper.sendMessage(event, "Missing arguments from command!");
		}
	}

}
