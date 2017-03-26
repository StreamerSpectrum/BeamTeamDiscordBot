package com.StreamerSpectrum.BeamTeamDiscordBot.beam.services;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.response.TeamUserSearchResponse;
import com.google.common.util.concurrent.ListenableFuture;

import pro.beam.api.BeamAPI;
import pro.beam.api.http.BeamHttpClient;
import pro.beam.api.services.AbstractHTTPService;

public class TeamsService extends AbstractHTTPService {

	public TeamsService(BeamAPI beam) {
		super(beam, "teams");
	}

	public ListenableFuture<BeamTeam> findOne(int id) {
		return this.get(String.valueOf(id), BeamTeam.class);
	}

	public ListenableFuture<BeamTeam> findOne(String token) {
		return this.get(token, BeamTeam.class);
	}

	public ListenableFuture<TeamUserSearchResponse> teamMembersOf(BeamTeam team, int page, int limit) {
		limit = limit < 1 ? 1 : limit > 50 ? 50 : limit;
		page = page < 0 ? 0 : page;
		
		return this.get(String.format("%d/users", team.id), TeamUserSearchResponse.class,
				BeamHttpClient.getArgumentsBuilder().put("id", team.id).put("page", page).put("limit", limit).build());
	}
}
