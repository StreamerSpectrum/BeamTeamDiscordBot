package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.ratelimit.RateLimit;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.response.TeamUserSearchResponse;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.services.TeamsService;

import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.channel.BeamChannel;
import pro.beam.api.resource.constellation.BeamConstellation;
import pro.beam.api.response.users.UserFollowsResponse;
import pro.beam.api.services.impl.UsersService;

public abstract class BeamManager {

	private static BeamAPI				beam;
	private static BeamConstellation	constellation;

	private static RateLimit			userReadLimit;

	public static BeamAPI getBeam() {
		if (null == beam) {
			beam = new BeamAPI();

			beam.register(new TeamsService(beam));
		}

		return beam;
	}

	public static BeamConstellation getConstellation() {
		if (null == constellation) {
			constellation = new BeamConstellation();
		}

		return constellation;
	}

	private static RateLimit getUserReadLimit() {
		if (null == userReadLimit) {
			userReadLimit = new RateLimit(500, 60);
		}

		return userReadLimit;
	}

	public static BeamUser getUser(int id) throws InterruptedException, ExecutionException {
		return getBeam().use(UsersService.class).findOne(id).get();
	}

	public static BeamUser getUser(String name) throws InterruptedException, ExecutionException {
		return getBeam().use(UsersService.class).search(name).get().get(0);
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
		int page = 0;

		while (teamMembers.addAll(getBeam().use(TeamsService.class).teamMembersOf(team, page++, 50).get()));

		return teamMembers;
	}

	public static List<BeamChannel> getFollowing(int id) throws InterruptedException, ExecutionException {
		return getFollowing(getUser(id));
	}

	public static List<BeamChannel> getFollowing(BeamTeamUser user) throws InterruptedException, ExecutionException {
		return getFollowing(getUser(user.id));
	}

	public static List<BeamChannel> getFollowing(BeamUser user) throws InterruptedException, ExecutionException {
		UserFollowsResponse following = new UserFollowsResponse();

		int page = 0;

		while (getUserReadLimit().isNotLimited()
				&& following.addAll(getBeam().use(UsersService.class).following(user, page++, 50).get()));

		return following;
	}
}
