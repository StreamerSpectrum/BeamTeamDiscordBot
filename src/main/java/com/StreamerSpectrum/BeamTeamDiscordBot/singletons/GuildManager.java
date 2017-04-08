package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public abstract class GuildManager {

	private static Map<Long, Integer> guildShardIDs;

	public static void init() {
		// TODO: populate guildShardIDs on login
		try {
			List<Guild> guilds = JDAManager.getJDA().getGuilds();

			for (Guild guild : guilds) {
				getGuildShardIDs().put(Long.parseLong(guild.getId()), JDAManager.getJDA().getShardInfo() != null
						? JDAManager.getJDA().getShardInfo().getShardId() : 0);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RateLimitedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static Map<Long, Integer> getGuildShardIDs() {
		if (null == guildShardIDs) {
			guildShardIDs = new HashMap<Long, Integer>();
		}

		return guildShardIDs;
	}

	public static void addGuild(BTBGuild guild) {
		try {
			DbManager.createGuild(guild);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getShardID(long guildID) {
		return getGuildShardIDs().isEmpty() ? 0 : getGuildShardIDs().get(guildID);
	}

	public static void deleteGuild(long id) {
		try {
			DbManager.deleteGuild(id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static BTBGuild getGuild(long id) {
		BTBGuild guild = null;

		try {
			guild = DbManager.readGuild(id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return guild;
	}

	public static BTBGuild getGuild(Guild guild) {
		long id = Long.parseLong(guild.getId());
		BTBGuild storedGuild = getGuild(id);

		if (storedGuild == null) {
			storedGuild = new BTBGuild(id,
					guild.getJDA().getShardInfo() == null ? 0 : guild.getJDA().getShardInfo().getShardId(), null, null,
					false);
			addGuild(storedGuild);
		}

		return storedGuild;
	}
}
