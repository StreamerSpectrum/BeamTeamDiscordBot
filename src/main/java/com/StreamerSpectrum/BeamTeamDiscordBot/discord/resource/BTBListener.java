package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

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

				List<BTBRole> roles = DbManager.readTeamRolesForGuild(Long.parseLong(gmje.getGuild().getId()));

				for (BTBRole role : roles) {
					List<BeamTeamUser> teamMembers = BeamManager.getTeamMembers(role.getTeamID());

					for (BeamTeamUser member : teamMembers) {
						if (null != member.social && StringUtils.isNotBlank(member.social.discord)) {
							if (StringUtils.containsIgnoreCase(member.social.discord,
									gmje.getMember().getUser().getName())) {
								JDAManager.giveTeamRoleToUser(role, gmje.getMember().getUser());
								break;
							}
						}
					}
				}
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
