package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.constellation.Constellation;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;;

public class BTBGuild {

	private final long	id;
	private final int	shardID;

	private String		goLiveChannelID;

	public BTBGuild(long id, int shardID, String goLiveChannelID) {
		this.id = id;
		this.shardID = shardID;
		this.goLiveChannelID = goLiveChannelID;
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

		if (StringUtils.isNotBlank(goLiveChannelID)) {
			subscribeAllTracked();
		} else {
			unsubscribeAllTracked();
		}

		try {
			DbManager.update(Constants.TABLE_GUILDS, getDbValues(),
					String.format("%s = %d", Constants.GUILDS_COL_ID, id));
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

	private Constellation getConstellation() {
		return ConstellationManager.getConstellation(id);
	}

	public void subscribeAllTracked() {
		List<BeamTeam> teams = getTrackedTeams();
		for (BeamTeam team : teams) {
			addTeamToConstellation(team);
		}

		List<BTBBeamChannel> channels = getTrackedChannels();
		for (BTBBeamChannel channel : channels) {
			addChannelToConstellation(channel);
		}
	}

	public void unsubscribeAllTracked() {
		List<BeamTeam> teams = getTrackedTeams();
		for (BeamTeam team : teams) {
			removeTeamFromConstellation(team);
		}

		List<BTBBeamChannel> channels = getTrackedChannels();
		for (BTBBeamChannel channel : channels) {
			removeChannelFromConstellation(channel);
		}
	}

	public List<BeamTeam> getTrackedTeams() {
		List<BeamTeam> teams = new ArrayList<>();

		try {
			List<List<String>> values = DbManager.read(Constants.TABLE_TEAMS, null,
					String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDTEAMS, Constants.TABLE_TEAMS,
							Constants.TEAMS_COL_ID, Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_TEAMID),
					String.format("%s.%s = %d", Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_GUILDID, id));

			for (List<String> valList : values) {
				BeamTeam team = new BeamTeam();

				team.id = Integer.parseInt(valList.get(0));
				team.name = valList.get(1);
				team.token = valList.get(2);

				teams.add(team);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return teams;
	}

	public List<BTBBeamChannel> getTrackedChannels() {
		List<BTBBeamChannel> channels = new ArrayList<>();

		try {
			List<List<String>> values = DbManager.read(Constants.TABLE_CHANNELS, null,
					String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDCHANNELS, Constants.TABLE_CHANNELS,
							Constants.CHANNELS_COL_ID, Constants.TABLE_TRACKEDCHANNELS,
							Constants.TRACKEDCHANNELS_COL_CHANNELID),
					String.format("%s.%s = %d", Constants.TABLE_TRACKEDCHANNELS, Constants.TRACKEDCHANNELS_COL_GUILDID,
							id));

			for (List<String> valList : values) {
				BTBBeamChannel channel = new BTBBeamChannel();

				channel.id = Integer.parseInt(valList.get(0));
				channel.user = new BTBBeamUser();
				channel.user.username = valList.get(1);
				channel.token = valList.get(2);
				channel.userId = Integer.parseInt(valList.get(3));

				channels.add(channel);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return channels;
	}

	public boolean addTeam(BeamTeam team) {
		boolean added = false;

		Map<String, Object> values = new HashMap<>();
		values.put(Constants.TRACKEDTEAMS_COL_GUILDID, id);
		values.put(Constants.TRACKEDTEAMS_COL_TEAMID, team.id.intValue());

		try {
			DbManager.create(Constants.TABLE_TEAMS, team.getDbValues());
			added = DbManager.create(Constants.TABLE_TRACKEDTEAMS, values);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (added) {
			// TODO: check if go-live or log channel is set, then subscribe all
			// team members to constellation for go-live
			addTeamToConstellation(team);
			// TODO: check if new member announce channel is set, then subscribe
			// to team member join
			// TODO: check if log channel is set, then subscribe to team member
			// leave
		}

		return added;
	}

	public boolean hasTeam(BeamTeam team) {
		boolean hasTeam = false;

		try {
			hasTeam = !DbManager.read(Constants.TABLE_TRACKEDTEAMS, null, null,
					String.format("%s = %d", Constants.TRACKEDTEAMS_COL_GUILDID, id)).isEmpty();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hasTeam;
	}

	public boolean removeTeam(BeamTeam team) {
		boolean isDeleted = false;

		try {
			isDeleted = DbManager.delete(Constants.TABLE_TRACKEDTEAMS, String.format("%s = %d AND %s = %d",
					Constants.TRACKEDTEAMS_COL_GUILDID, id, Constants.TRACKEDTEAMS_COL_TEAMID, team.id));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isDeleted) {
			removeTeamFromConstellation(team);
		}

		return isDeleted;
	}

	public boolean addChannel(BTBBeamChannel channel) {
		boolean added = false;

		Map<String, Object> values = new HashMap<>();
		values.put(Constants.TRACKEDCHANNELS_COL_GUILDID, id);
		values.put(Constants.TRACKEDCHANNELS_COL_CHANNELID, channel.id);

		try {
			DbManager.create(Constants.TABLE_CHANNELS, channel.getDbValues());
			added = DbManager.create(Constants.TABLE_TRACKEDCHANNELS, values);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (added) {
			// TODO: check if go-live or log channel is set, then subscribe all
			// team members to constellation for go-live
			addChannelToConstellation(channel);
			// TODO: check if new member announce channel is set, then subscribe
			// to team member join
			// TODO: check if log channel is set, then subscribe to team member
			// leave
		}

		return added;
	}

	public boolean hasChannel(BTBBeamChannel channel) {
		boolean hasChannel = false;

		try {
			hasChannel = !DbManager.read(Constants.TABLE_TRACKEDCHANNELS, null, null,
					String.format("%s = %d", Constants.TRACKEDCHANNELS_COL_GUILDID, id)).isEmpty();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hasChannel;
	}

	public boolean removeChannel(BTBBeamChannel channel) {
		boolean isDeleted = false;

		try {
			isDeleted = DbManager.delete(Constants.TABLE_TRACKEDCHANNELS, String.format("%s = %d AND %s = %d",
					Constants.TRACKEDCHANNELS_COL_GUILDID, id, Constants.TRACKEDCHANNELS_COL_CHANNELID, channel.id));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isDeleted) {
			removeChannelFromConstellation(channel);
		}

		return isDeleted;
	}

	private void addTeamToConstellation(BeamTeam team) {
		if (StringUtils.isNotBlank(getGoLiveChannelID())) {
			List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

			for (BeamTeamUser member : members) {
				getConstellation().subscribeToChannel(member.channel.id);
				System.out.println(String.format("Subscribed to %s", member.username));
			}
		}
	}

	private void removeTeamFromConstellation(BeamTeam team) {
		List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

		for (BeamTeamUser member : members) {
			getConstellation().unsubscribeFromChannel(member.channel.id);
			System.out.println(String.format("Unsubscribed from %s", member.username));
		}
	}

	private void addChannelToConstellation(BTBBeamChannel channel) {
		if (StringUtils.isNotBlank(getGoLiveChannelID())) {
			getConstellation().subscribeToChannel(channel.id);
			System.out.println(String.format("Subscribed to %s", channel.user.username));
		}
	}

	private void removeChannelFromConstellation(BTBBeamChannel channel) {
		getConstellation().unsubscribeFromChannel(channel.id);
		System.out.println(String.format("Unsubscribed from %s", channel.user.username));
	}
}
