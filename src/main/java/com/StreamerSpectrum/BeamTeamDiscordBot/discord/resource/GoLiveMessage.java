package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.util.HashMap;
import java.util.Map;

import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;

public class GoLiveMessage {

	private final String	messageID;
	private final String	goLiveChannelID;
	private final long		guildID;
	private final int		beamChannelID;

	public GoLiveMessage(String messageID, String goLiveChannelID, long guildID, int beamChannelID) {
		this.messageID = messageID;
		this.goLiveChannelID = goLiveChannelID;
		this.guildID = guildID;
		this.beamChannelID = beamChannelID;
	}

	public String getMessageID() {
		return messageID;
	}

	public String getGoLiveChannelID() {
		return goLiveChannelID;
	}

	public long getGuildID() {
		return guildID;
	}

	public int getBeamChannelID() {
		return beamChannelID;
	}

	public Map<String, Object> getDbValues() {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.GOLIVEMESSAGES_COL_ID, messageID);
		values.put(Constants.GOLIVEMESSAGES_COL_GOLIVECHANNELID, goLiveChannelID);
		values.put(Constants.GOLIVEMESSAGES_COL_GUILDID, guildID);
		values.put(Constants.GOLIVEMESSAGES_COL_BEAMCHANNELID, beamChannelID);

		return values;
	}
}
