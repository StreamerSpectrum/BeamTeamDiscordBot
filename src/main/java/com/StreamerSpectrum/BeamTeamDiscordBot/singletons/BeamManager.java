package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.response.TeamUserSearchResponse;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.services.TeamsService;

import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.services.impl.UsersService;

public class BeamManager {
	
	private static BeamAPI beam;
	
	public static BeamAPI getBeam() {
		if (null == beam) {
			beam = new BeamAPI();
			
			beam.register(new TeamsService(beam));
		}
		
		return beam;	
	}
	
	public static BeamUser getUser(int id) throws InterruptedException, ExecutionException {
		return getBeam().use(UsersService.class).findOne(id).get();
	}
	
	public static BeamTeam getTeam(String team) throws InterruptedException, ExecutionException {
		return getBeam().use(TeamsService.class).findOne(team).get();
	}
	
	public static BeamTeam getTeam(int id) throws InterruptedException, ExecutionException {
		return getBeam().use(TeamsService.class).findOne(id).get();
	}
	
	public static List<BeamTeamUser> getTeamMembers(String team) throws InterruptedException, ExecutionException {
		return getTeamMembers(getTeam(team));
	}
	
	public static List<BeamTeamUser> getTeamMembers(int id) throws InterruptedException, ExecutionException {
		return getTeamMembers(getTeam(id));
	}
	
	public static List<BeamTeamUser> getTeamMembers(BeamTeam team) throws InterruptedException, ExecutionException {
		TeamUserSearchResponse teamMembers = new TeamUserSearchResponse();
		int page = 0, limit = 50;
		
		do {
			teamMembers.addAll(getBeam().use(TeamsService.class).teamMembersOf(team, page++, limit).get());
		} while (!getBeam().use(TeamsService.class).teamMembersOf(team, page, limit).get().isEmpty());
		
		return teamMembers;
	}
}
