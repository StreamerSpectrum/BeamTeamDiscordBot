package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;

public class Tracker {

	private final Guild					owner;

	private Map<Integer, BeamTeam>		teams;
	private Map<Integer, BTBBeamChannel>	channels;

	public Tracker(Guild owner) {
		this.owner = owner;
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

		if (added && StringUtils.isNotBlank(owner.getOptions().getGoLiveChannelID())) {
			owner.getConstellation().subscribeToChannel(channel.id);
		}

		return added;
	}

	public boolean hasChannel(BTBBeamChannel channel) {
		return getChannels().containsKey(channel.id);
	}

	public boolean removeChannel(BTBBeamChannel channel) {
		boolean removed = getChannels().remove(channel.id) != null;

		if (removed) {
			owner.getConstellation().unsubscribeFromChannel(channel.id);
		}

		return removed;
	}

	private void addTeamToConstellation(BeamTeam team) {
		if (StringUtils.isNotBlank(owner.getOptions().getGoLiveChannelID())) {
			List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

			for (BeamTeamUser member : members) {
				owner.getConstellation().subscribeToChannel(member.channel.id);
			}
		}
	}

	private void removeTeamFromConstellation(BeamTeam team) {
		List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

		if (!members.isEmpty()) {
			for (BeamTeamUser member : members) {
				owner.getConstellation().unsubscribeFromChannel(member.channel.id);
			}
		}
	}
}
