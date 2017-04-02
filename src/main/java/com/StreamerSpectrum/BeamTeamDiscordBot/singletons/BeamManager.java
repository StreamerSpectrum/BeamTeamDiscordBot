package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
import pro.beam.api.services.impl.ChannelsService;
import pro.beam.api.services.impl.UsersService;

public abstract class BeamManager {

	private static BeamAPI				beam;
	private static BeamConstellation	constellation;

	private static RateLimit			userReadLimit, channelReadLimit;

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

	private static RateLimit getChannelReadLimit() {
		if (null == channelReadLimit) {
			channelReadLimit = new RateLimit(1000, 300);
		}

		return channelReadLimit;
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

	public static List<BeamTeamUser> getTeamMembers(BeamTeam team) {
		TeamUserSearchResponse teamMembers = new TeamUserSearchResponse();		
		int page = 0;

		try {
			while (teamMembers.addAll(getBeam().use(TeamsService.class).teamMembersOf(team, page++, 50).get()));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Send a log message to the log channel
			e.printStackTrace();
		}

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

	public static BeamChannel getChannel(int id) throws InterruptedException, ExecutionException {
		BeamChannel channel = null;

		for (int i = 0; channel == null && i < 100; ++i) {
			if (getChannelReadLimit().isNotLimited()) {
				channel = getBeam().use(ChannelsService.class).findOne(id).get();
			} else {
				break;
			}

			if (channel == null) {
				TimeUnit.SECONDS.sleep(5);
			}
		}

		return channel;
	}

	public static BeamChannel getChannel(String name) throws InterruptedException, ExecutionException {
		BeamChannel channel = null;

		for (int i = 0; channel == null && i < 100; ++i) {
			if (getChannelReadLimit().isNotLimited()) {
				channel = getBeam().use(ChannelsService.class).findOneByToken(name).get();
			} else {
				break;
			}

			if (channel == null) {
				TimeUnit.SECONDS.sleep(5);
			}
		}

		return channel;
	}
}
