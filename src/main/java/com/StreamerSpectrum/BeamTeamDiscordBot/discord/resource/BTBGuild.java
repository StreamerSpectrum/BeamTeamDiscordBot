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

	private String		name;
	private String		goLiveChannelID						= null;
	private String		logChannelID						= null;

	private boolean		removeOfflineChannelAnnouncements	= false;

	public BTBGuild(long id, int shardID, String name, String goLiveChannelID, String logChannelID,
			boolean removeOfflineChannels) {
		this.id = id;
		this.shardID = shardID;
		this.name = name;
		this.goLiveChannelID = goLiveChannelID;
		this.logChannelID = logChannelID;
		this.removeOfflineChannelAnnouncements = removeOfflineChannels;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;

		update();
	}

	public String getGoLiveChannelID() {
		return goLiveChannelID;
	}

	public void setGoLiveChannelID(String goLiveChannelID) {
		this.goLiveChannelID = goLiveChannelID;

		update();
	}

	public String getLogChannelID() {
		return logChannelID;
	}

	public void setLogChannelID(String logChannelID) {
		this.logChannelID = logChannelID;

		update();
	}

	public boolean isRemoveOfflineChannelAnnouncements() {
		return removeOfflineChannelAnnouncements;
	}

	public void setRemoveOfflineChannelAnnouncements(boolean removeOfflineChannelAnnouncements) {
		this.removeOfflineChannelAnnouncements = removeOfflineChannelAnnouncements;

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

		values.put(Constants.GUILDS_COL_ID, getID());
		values.put(Constants.GUILDS_COL_NAME, getName());
		values.put(Constants.GUILDS_COL_GOLIVECHANNELID, getGoLiveChannelID());
		values.put(Constants.GUILDS_COL_LOGCHANNELID, getLogChannelID());
		values.put(Constants.GUILDS_COL_REMOVEOFFLINECHANNELANNOUNCEMENTS,
				isRemoveOfflineChannelAnnouncements() ? 1 : 0);

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
			ConstellationManager.subscribeToChannel(channel);
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

	public void addRole(String string, String string2) {
		// TODO Auto-generated method stub
		
	}
}
