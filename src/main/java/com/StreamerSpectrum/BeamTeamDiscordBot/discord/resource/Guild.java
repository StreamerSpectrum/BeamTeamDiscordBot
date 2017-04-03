package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.constellation.Constellation;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;;

public class Guild {

	private final long						id;
	private final int						shardID;

	private Constellation					constellation;

	private String							goLiveChannelID;

	private Map<Integer, BeamTeam>			teams;
	private Map<Integer, BTBBeamChannel>	channels;

	public Guild(long id, int shardID) {
		this.id = id;
		this.shardID = shardID;
	}

	public long getID() {
		return id;
	}

	public int getShardID() {
		return shardID;
	}

	public Constellation getConstellation() {
		if (null == constellation) {
			constellation = new Constellation(id);

			// TODO: subscribe to team members and stuff if certain options are
			// set
		}

		return constellation;
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
	}

	private void subscribeAllTracked() {
		for (BeamTeam team : getTeams().values()) {
			List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

			for (BeamTeamUser member : members) {
				getConstellation().subscribeToChannel(member.channel.id);
			}
		}

		for (BTBBeamChannel channel : getChannels().values()) {
			getConstellation().subscribeToChannel(channel.id);
		}
	}

	private void unsubscribeAllTracked() {
		for (BeamTeam team : getTeams().values()) {
			List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

			for (BeamTeamUser member : members) {
				getConstellation().unsubscribeFromChannel(member.channel.id);
			}
		}

		for (BTBBeamChannel channel : getChannels().values()) {
			getConstellation().unsubscribeFromChannel(channel.id);
		}
	}

	public Map<Integer, BeamTeam> getTeams() {
		if (null == teams) {
			teams = new HashMap<Integer, BeamTeam>();
		}

		return teams;
	}

	public Map<Integer, BTBBeamChannel> getChannels() {
		if (null == channels) {
			channels = new HashMap<Integer, BTBBeamChannel>();
		}

		return channels;
	}

	public boolean addTeam(BeamTeam team) {
		boolean added = !hasTeam(team) && getTeams().put(team.id, team) == null;

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
		return getTeams().containsKey(team.id);
	}

	public boolean removeTeam(BeamTeam team) {
		boolean removed = getTeams().remove(team.id) != null;

		if (removed) {
			removeTeamFromConstellation(team);
		}

		return removed;
	}

	public boolean addChannel(BTBBeamChannel channel) {
		boolean added = !hasChannel(channel) && getChannels().put(channel.id, channel) == null;

		if (added && StringUtils.isNotBlank(getGoLiveChannelID())) {
			getConstellation().subscribeToChannel(channel.id);
		}

		return added;
	}

	public boolean hasChannel(BTBBeamChannel channel) {
		return getChannels().containsKey(channel.id);
	}

	public boolean removeChannel(BTBBeamChannel channel) {
		boolean removed = getChannels().remove(channel.id) != null;

		if (removed) {
			getConstellation().unsubscribeFromChannel(channel.id);
		}

		return removed;
	}

	private void addTeamToConstellation(BeamTeam team) {
		if (StringUtils.isNotBlank(getGoLiveChannelID())) {
			List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

			for (BeamTeamUser member : members) {
				getConstellation().subscribeToChannel(member.channel.id);
			}
		}
	}

	private void removeTeamFromConstellation(BeamTeam team) {
		List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

		if (!members.isEmpty()) {
			for (BeamTeamUser member : members) {
				getConstellation().unsubscribeFromChannel(member.channel.id);
			}
		}
	}

}
