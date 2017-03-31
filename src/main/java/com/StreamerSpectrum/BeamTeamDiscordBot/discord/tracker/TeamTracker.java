package com.StreamerSpectrum.BeamTeamDiscordBot.discord.tracker;

import java.util.HashMap;
import java.util.Map;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;

public class TeamTracker {

	Map<Integer, BeamTeam> teams;

	public Map<Integer, BeamTeam> getTeams() {
		if (null == teams) {
			teams = new HashMap<Integer, BeamTeam>();
		}

		return teams;
	}

	public boolean add(BeamTeam team) {
		boolean added = !hasTeam(team) && getTeams().put(team.id, team) == null;

		if (added) {
			// TODO: check if go-live or log channel is set, then subscribe all
			// team members to constellation for go-live
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

	public BeamTeam removeTeam(BeamTeam team) {
		return getTeams().remove(team.id);
	}
}
