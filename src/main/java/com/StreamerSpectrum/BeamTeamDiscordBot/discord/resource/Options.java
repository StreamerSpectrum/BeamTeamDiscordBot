package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;

import pro.beam.api.resource.channel.BeamChannel;

public class Options {

	private final Guild	owner;

	private String		goLiveChannelID;

	public Options(Guild owner) {
		this.owner = owner;
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
		for (BeamTeam team : owner.getTracker().getTeams().values()) {
			List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

			for (BeamTeamUser member : members) {
				owner.getConstellation().subscribeToChannel(member.channel.id);
			}
		}

		for (BeamChannel channel : owner.getTracker().getChannels().values()) {
			owner.getConstellation().subscribeToChannel(channel.id);
		}
	}

	private void unsubscribeAllTracked() {
		for (BeamTeam team : owner.getTracker().getTeams().values()) {
			List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

			for (BeamTeamUser member : members) {
				owner.getConstellation().unsubscribeFromChannel(member.channel.id);
			}
		}

		for (BeamChannel channel : owner.getTracker().getChannels().values()) {
			owner.getConstellation().unsubscribeFromChannel(channel.id);
		}
	}
}
