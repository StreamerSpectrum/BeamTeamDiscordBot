package com.StreamerSpectrum.BeamTeamDiscordBot;

import java.sql.SQLException;
import java.util.List;

import javax.security.auth.login.LoginException;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;

import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class BTBMain {

	public static void main(String[] args) throws LoginException, RateLimitedException, InterruptedException {
		JDAManager.getJDA(); // TODO: Replace this with the ShardManager.
		load();
	}

	public static void load() {

		try {
			List<List<String>> guilds = DbManager.read(Constants.TABLE_GUILDS, null, null,
					String.format("%s NOT NULL", Constants.GUILDS_COL_GOLIVECHANNELID));

			for (List<String> guild : guilds) {
				new BTBGuild(Long.parseLong(guild.get(0)), GuildManager.getShardID(Long.parseLong(guild.get(0))),
						guild.get(1)).subscribeAllTracked();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
