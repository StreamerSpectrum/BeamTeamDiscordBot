package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.util.HashMap;
import java.util.Map;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.constellation.Constellation;

public abstract class ConstellationManager {

	private static Map<Long, Constellation> constellationObjs;

	private static Map<Long, Constellation> getConstellationObjs() {
		if (null == constellationObjs) {
			constellationObjs = new HashMap<Long, Constellation>();
		}

		return constellationObjs;
	}

	public static Constellation addConstellation(long guildID) {
		if (!getConstellationObjs().containsKey(guildID)) {
			getConstellationObjs().put(guildID, new Constellation(guildID));
		}

		return getConstellationObjs().get(guildID);
	}

	public static Constellation removeConstellation(long guildID) {
		getConstellationObjs().get(guildID).disconnect();
		return getConstellationObjs().remove(guildID);
	}

	public static Constellation getConstellation(long guildID) {
		if (getConstellationObjs().get(guildID) == null) {
			addConstellation(guildID);
		}

		return getConstellationObjs().get(guildID);
	}
	
	public static void restartConstellation(long guildID) {
		removeConstellation(guildID);
		addConstellation(guildID);
		
		GuildManager.getGuild(guildID).subscribeAllTracked();
	}
}
