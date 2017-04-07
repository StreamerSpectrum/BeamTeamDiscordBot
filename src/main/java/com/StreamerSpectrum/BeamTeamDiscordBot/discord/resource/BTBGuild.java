package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;;

public class BTBGuild {

	private final long	id;
	private final int	shardID;

	private String		goLiveChannelID = null;

	public BTBGuild(long id, int shardID, String goLiveChannelID) {
		this.id = id;
		this.shardID = shardID;
		this.goLiveChannelID = goLiveChannelID;
	}

	@Override
	public int hashCode() {
		return new Long(id).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BTBGuild && ((BTBGuild) obj).id == this.id;
	}

	public long getID() {
		return id;
	}

	public int getShardID() {
		return shardID;
	}

	public String getGoLiveChannelID() {
		return goLiveChannelID;
	}

	public void setGoLiveChannelID(String goLiveChannelID) {
		this.goLiveChannelID = goLiveChannelID;

		update();
	}

	private void update() {
		try {
			DbManager.updateGuild(this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, Object> getDbValues() {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.GUILDS_COL_ID, id);
		values.put(Constants.GUILDS_COL_GOLIVECHANNELID, goLiveChannelID);

		return values;
	}

	public List<BeamTeam> getTrackedTeams() {
		List<BeamTeam> teams = new ArrayList<>();

		try {
			teams = DbManager.readTrackedTeamsForGuild(id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return teams;
	}

	public List<BTBBeamChannel> getTrackedChannels() {
		List<BTBBeamChannel> channels = new ArrayList<>();

		try {
			channels = DbManager.readTrackedChannelsForGuild(id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return channels;
	}

	public boolean addTeam(BeamTeam team) {
		boolean added = false;

		try {
			if (DbManager.readTeam(team.id) == null) {
				DbManager.createTeam(team);
			}

			added = DbManager.createTrackedTeam(id, team.id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (added) {
			ConstellationManager.subscribeToTeam(team);
		}

		return added;
	}

	public boolean removeTeam(BeamTeam team) {
		boolean isDeleted = false;

		try {
			isDeleted = DbManager.deleteTrackedTeam(id, team.id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return isDeleted;
	}

	public boolean addChannel(BTBBeamChannel channel) {
		boolean added = false;

		try {
			if (DbManager.readChannel(channel.id) == null) {
				DbManager.createChannel(channel);
			}

			added = DbManager.createTrackedChannel(id, channel.id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (added) {
			ConstellationManager.subscribeToChannel(channel.id);
		}

		return added;
	}

	public boolean removeChannel(BTBBeamChannel channel) {
		boolean isDeleted = false;

		try {
			isDeleted = DbManager.deleteTrackedChannel(id, channel.id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return isDeleted;
	}
}
