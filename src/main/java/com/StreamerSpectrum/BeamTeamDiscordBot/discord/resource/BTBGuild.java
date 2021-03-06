package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.ConstellationManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.DbManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;
import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;

public class BTBGuild {

	private final long	id;
	private final int	shardID;

	private String		name;
	private String		goLiveChannelID						= null;
	private String		logChannelID						= null;
	private String		newMemberChannelID					= null;

	private boolean		removeOfflineChannelAnnouncements	= false;

	public BTBGuild(long id, int shardID, String name, String goLiveChannelID, String logChannelID,
			String newMemberChannelID, boolean removeOfflineChannels) {
		this.id = id;
		this.shardID = shardID;
		this.name = name;
		this.goLiveChannelID = goLiveChannelID;
		this.logChannelID = logChannelID;
		this.newMemberChannelID = newMemberChannelID;
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

		if (StringUtils.isNotBlank(goLiveChannelID)) {
			Set<BTBBeamChannel> alreadyAnnounced = new HashSet<>();
			List<BeamTeam> teams = getTrackedTeams();

			for (BeamTeam team : teams) {
				List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

				for (BeamTeamUser member : members) {
					if (member.channel.online && !alreadyAnnounced.contains(member.channel)) {
						BTBBeamChannel channel = BeamManager.getChannel(member.channel.id);

						sendGoLiveMessage(channel);

						alreadyAnnounced.add(channel);
					}
				}
			}

			List<BTBBeamChannel> channels = getTrackedChannels();

			for (BTBBeamChannel channel : channels) {
				if (!alreadyAnnounced.contains(channel)) {
					channel = BeamManager.getChannel(channel.id);

					if (channel.online) {
						sendGoLiveMessage(channel);

						alreadyAnnounced.add(channel);
					}
				}
			}
		}

		update();
	}

	public String getLogChannelID() {
		return logChannelID;
	}

	public void setLogChannelID(String logChannelID) {
		this.logChannelID = logChannelID;

		update();
	}

	public String getNewMemberChannelID() {
		return newMemberChannelID;
	}

	public void setNewMemberChannelID(String newMemberChannelID) {
		this.newMemberChannelID = newMemberChannelID;

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
		DbManager.updateGuild(this);
	}

	public Map<String, Object> getDbValues() {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.GUILDS_COL_ID, getID());
		values.put(Constants.GUILDS_COL_NAME, getName());
		values.put(Constants.GUILDS_COL_GOLIVECHANNELID, getGoLiveChannelID());
		values.put(Constants.GUILDS_COL_LOGCHANNELID, getLogChannelID());
		values.put(Constants.GUILDS_COL_NEWMEMBERCHANNELID, getNewMemberChannelID());
		values.put(Constants.GUILDS_COL_REMOVEOFFLINECHANNELANNOUNCEMENTS,
				isRemoveOfflineChannelAnnouncements() ? 1 : 0);

		return values;
	}

	public List<BeamTeam> getTrackedTeams() {
		List<BeamTeam> teams = new ArrayList<>();

		teams = DbManager.readTrackedTeamsForGuild(id);

		return teams;
	}

	public List<BTBBeamChannel> getTrackedChannels() {
		List<BTBBeamChannel> channels = new ArrayList<>();

		channels = DbManager.readTrackedChannelsForGuild(id);

		return channels;
	}

	public boolean addTeam(BeamTeam team) {
		boolean added = false;

		if (DbManager.readTeam(team.id) == null) {
			DbManager.createTeam(team);
		}

		added = DbManager.createTrackedTeam(id, team.id);

		if (added) {
			if (StringUtils.isNotBlank(getGoLiveChannelID())) {
				List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

				for (BeamTeamUser member : members) {
					if (member.channel.online) {
						sendGoLiveMessage(BeamManager.getChannel(member.channel.id));
					}
				}
			}

			ConstellationManager.subscribeToTeam(team);
		}

		return added;
	}

	public boolean removeTeam(BeamTeam team) {
		boolean isDeleted = false;

		isDeleted = DbManager.deleteTrackedTeam(id, team.id);

		return isDeleted;
	}

	public boolean addChannel(BTBBeamChannel channel) {
		boolean added = false;

		if (DbManager.readChannel(channel.id) == null) {
			DbManager.createChannel(channel);
		}

		added = DbManager.createTrackedChannel(id, channel.id);

		if (added) {
			if (channel.online) {
				sendGoLiveMessage(channel);
			}
			ConstellationManager.subscribeToChannel(channel);
		}

		return added;
	}

	public boolean removeChannel(BTBBeamChannel channel) {
		boolean isDeleted = false;

		isDeleted = DbManager.deleteTrackedChannel(id, channel.id);

		return isDeleted;
	}

	public void sendGoLiveMessage(BTBBeamChannel channel) {
		if (StringUtils.isNotBlank(getGoLiveChannelID())) {
			JDAManager.sendGoLiveMessage(getGoLiveChannelID(), JDAManager.buildGoLiveEmbed(channel), channel);
		}
	}

	public void sendLogMessage(String text) {
		if (StringUtils.isNotBlank(getLogChannelID())) {
			JDAManager.sendMessage(getLogChannelID(),
					String.format("[%s] %s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()), text));
		}
	}
}
