package com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.newmember;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class NewMemberRemove extends Command {

	public NewMemberRemove() {
		this.name = "newmemberremove";
		this.help = "Removes the channel for new member announcements.";
		this.userPermissions = new Permission[] { Permission.MANAGE_CHANNEL };
	}

	@Override
	protected void execute(CommandEvent event) {
		BTBGuild guild = GuildManager.getGuild(event.getGuild());
		if (guild.getNewMemberChannelID() != null) {
			guild.setNewMemberChannelID(null);

			JDAManager.sendMessage(event, "New member channel has been removed.");
		} else {
			JDAManager.sendMessage(event, "There is no new member channel set for this server.");
		}
	}

}
