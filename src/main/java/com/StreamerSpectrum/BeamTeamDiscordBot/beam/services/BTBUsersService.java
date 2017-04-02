package com.StreamerSpectrum.BeamTeamDiscordBot.beam.services;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.response.BTBUserFollowsResponse;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.response.BTBUserSearchResponse;
import com.google.common.util.concurrent.ListenableFuture;
import pro.beam.api.BeamAPI;
import pro.beam.api.http.BeamHttpClient;
import pro.beam.api.services.AbstractHTTPService;
import java.util.Map;

public class BTBUsersService extends AbstractHTTPService {
	public BTBUsersService(BeamAPI beam) {
		super(beam, "users");
	}

	public ListenableFuture<BTBBeamUser> findOne(int id) {
		return this.get(String.valueOf(id), BTBBeamUser.class);
	}

	public ListenableFuture<BTBUserSearchResponse> search(String query) {
		if (query != null && query.length() < 3) {
			throw new IllegalArgumentException(
					"unable to preform search with query less than 3 characters (was " + query.length() + ")");
		} else {
			Map<String, Object> args = BeamHttpClient.getArgumentsBuilder().put("query", query).build();

			return this.get("search", BTBUserSearchResponse.class, args);
		}
	}

	public ListenableFuture<BTBUserFollowsResponse> following(int id, int page, int limit) {
		return this.get(id + "/follows", BTBUserFollowsResponse.class, BeamHttpClient.getArgumentsBuilder()
				.put("page", Math.max(0, page)).put("limit", Math.min(limit, 50)).build());
	}
	
	public ListenableFuture<TeamMembershipExpandedSearchResponse> teams(int id) {
		return this.get(id + "/teams", TeamMembershipExpandedSearchResponse.class);
	}
}
