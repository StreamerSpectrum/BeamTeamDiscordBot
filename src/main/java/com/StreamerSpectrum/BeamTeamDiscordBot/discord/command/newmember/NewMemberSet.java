package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.newmember;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

public class NewMemberSet extends Command {

	public NewMemberSet() {
		this.name = "newmemberset";
		this.help = "Sets the channel for new member announcements.";
		this.arguments = "channel";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!StringUtils.isBlank(event.getArgs())) {
			String arg = event.getArgs().trim();

			List<TextChannel> textChannels = event.getJDA().getTextChannelsByName(arg, true);

			if (!textChannels.isEmpty()) {
				BTBGuild guild = GuildManager.getGuild(event.getGuild());

				guild.setNewMemberChannelID(textChannels.get(0).getId());

				JDAManager.sendMessage(event, "New member announce channel has been set to %s.", arg);				
			} else {
				JDAManager.sendMessage(event,
						"I can't find the channel named %s. Please ensure it exists and that I have read & write priveleges for it.",
						arg);
			}
		} else {
			JDAManager.sendMessage(event, "Missing arguments from command!");
		}
	}

}
