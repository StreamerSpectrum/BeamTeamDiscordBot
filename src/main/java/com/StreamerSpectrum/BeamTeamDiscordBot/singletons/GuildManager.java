package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.util.HashMap;
import java.util.Map;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.Guild;

public abstract class GuildManager {

	private static Map<Long, Guild> guilds;
	
	private static Map<Long, Guild> getGuilds() {
		if (null == guilds) {
			guilds = new HashMap<>();			
		}
		
		return guilds;
	}
	
	public static void addGuild(Guild guild) {
		getGuilds().put(guild.getID(), guild);
	}
	
	public static void deleteGuild(Guild guild) {
		getGuilds().remove(guild.getID());
	}
	
	public static Guild getGuild(String id) {
		return getGuild(Long.parseLong(id));
	}
	
	private static Guild getGuild(long id) {
		if (!getGuilds().containsKey(id)) {
			addGuild(new Guild(id));
		}
		
		return getGuilds().get(id);
	}
}
