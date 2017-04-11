package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;

import net.dv8tion.jda.core.entities.Guild;

public abstract class GuildManager {

	private static Map<Long, Integer> guildShardIDs;

	public static void init() {
		// TODO: populate guildShardIDs on login
		List<Guild> guilds = JDAManager.getJDA().getGuilds();

		for (Guild guild : guilds) {
			getGuildShardIDs().put(Long.parseLong(guild.getId()),
					JDAManager.getJDA().getShardInfo() != null ? JDAManager.getJDA().getShardInfo().getShardId() : 0);
		}

	}

	private static Map<Long, Integer> getGuildShardIDs() {
		if (null == guildShardIDs) {
			guildShardIDs = new HashMap<Long, Integer>();
		}

		return guildShardIDs;
	}

	public static void addGuild(BTBGuild guild) {
		DbManager.createGuild(guild);
	}

	public static int getShardID(long guildID) {
		return getGuildShardIDs().isEmpty() ? 0 : getGuildShardIDs().get(guildID);
	}

	public static void deleteGuild(long id) {
		DbManager.deleteGuild(id);
	}

	public static BTBGuild getGuild(long id) {
		BTBGuild guild = null;

		guild = DbManager.readGuild(id);

		return guild;
	}

	public static BTBGuild getGuild(Guild guild) {
		long id = Long.parseLong(guild.getId());
		BTBGuild storedGuild = getGuild(id);

		if (storedGuild == null) {
			storedGuild = new BTBGuild(id,
					guild.getJDA().getShardInfo() == null ? 0 : guild.getJDA().getShardInfo().getShardId(),
					guild.getName(), null, null, null, false);
			addGuild(storedGuild);
		}

		return storedGuild;
	}
}
