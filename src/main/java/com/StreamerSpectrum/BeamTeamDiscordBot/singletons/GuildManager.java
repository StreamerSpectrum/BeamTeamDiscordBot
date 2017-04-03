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

	public static Guild getGuild(long id) {
		return getGuilds().get(id);
	}

	public static Guild getGuild(net.dv8tion.jda.core.entities.Guild guild) {
		long id = Long.parseLong(guild.getId());

		if (!getGuilds().containsKey(id)) {
			addGuild(new Guild(id,
					guild.getJDA().getShardInfo() == null ? 0 : guild.getJDA().getShardInfo().getShardId()));
		}

		return getGuilds().get(id);
	}
}
