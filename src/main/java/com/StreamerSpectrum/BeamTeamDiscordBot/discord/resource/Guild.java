package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.util.Map;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.tracker.TeamTracker;

public class Guild {

	private final long	id;

	private TeamTracker	teamTracker;
	private Options		options;

	public Guild(long id) {
		this.id = id;
	}

	public long getID() {
		return id;
	}

	private TeamTracker getTeamTracker() {
		if (null == teamTracker) {
			teamTracker = new TeamTracker();

			// TODO: load stored teams from DB
		}

		return teamTracker;
	}

	private Options getOptions() {
		if (null == options) {
			options = new Options();

			// TODO: load stored options from DB
		}

		return options;
	}

	public Map<Integer, BeamTeam> getTeams() {
		return getTeamTracker().getTeams();
	}

	public boolean addTeam(BeamTeam team) {
		return getTeamTracker().add(team);
	}

	public boolean removeTeam(BeamTeam team) {
		return getTeamTracker().removeTeam(team) != null;
	}

}
