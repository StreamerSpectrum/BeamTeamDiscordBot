package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.Guild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

public class GoLiveSet extends Command {

	public GoLiveSet() {
		this.name = "goliveset";
		this.help = "Sets the target channel as the announcement channel for when tracked team members go live.";
		this.arguments = "channel";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String arg = event.getArgs().trim();

			try {
				List<TextChannel> textChannels = event.getJDA().getTextChannelsByName(arg, true);

				if (!textChannels.isEmpty()) {
					Guild guild = GuildManager.getGuild(event.getGuild());

					guild.setGoLiveChannelID(textChannels.get(0).getId());

					CommandHelper.sendMessage(event, "Go-live channel has been set to %s.", arg);
				} else {
					CommandHelper.sendMessage(event,
							"I can't find the channel named %s. Please ensure it exists and that I have read & write priveleges for it.",
							arg);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else {
			CommandHelper.sendMessage(event, "Missing arguments from command!");
		}
	}

}
