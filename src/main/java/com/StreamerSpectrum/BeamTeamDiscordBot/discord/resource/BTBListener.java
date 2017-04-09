package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class BTBListener implements EventListener {

	@Override
	public void onEvent(Event event) {

		if (event instanceof ReconnectedEvent || event instanceof ResumedEvent) {
			ConstellationManager.restartConstellation();
			System.out.println("Constellation has been restarted due to a Discord reconnect.");
		} else if (event instanceof GenericGuildEvent) {
			GenericGuildEvent gge = ((GenericGuildEvent) event);

			if (event instanceof GuildMemberJoinEvent) {
				GuildMemberJoinEvent gmje = ((GuildMemberJoinEvent) gge);
				// TODO: add role to member if guild has that configured
			} else if (event instanceof GuildJoinEvent) {
				GuildManager.getGuild(gge.getGuild());
				System.out.println(String.format("%s has added the bot, they have been added to the database.",
						gge.getGuild().getName()));
			} else if (event instanceof GuildLeaveEvent) {
				GuildManager.deleteGuild(Long.parseLong(gge.getGuild().getId()));
				System.out.println(String.format(
						"%s has removed the bot, they and all relevant records have been purged from the database.",
						gge.getGuild().getName()));
			}
		} else if (event instanceof RoleDeleteEvent) {
			// TODO: delete role config from DB
		}
	}

}
