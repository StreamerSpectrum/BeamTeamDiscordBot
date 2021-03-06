package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.ratelimit.RateLimit;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.TeamMembershipExpanded;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.response.BTBUserFollowsResponse;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.response.TeamUserSearchResponse;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.services.BTBChannelsService;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.services.BTBUsersService;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.services.TeamsService;

import pro.beam.api.BeamAPI;
import pro.beam.api.resource.constellation.BeamConstellation;

public abstract class BeamManager {

	private static BeamAPI				beam;
	private static BeamConstellation	constellation;

	private static RateLimit			userReadLimit, channelReadLimit;

	public static BeamAPI getBeam() {
		if (null == beam) {
			beam = new BeamAPI();

			beam.register(new TeamsService(beam));
			beam.register(new BTBChannelsService(beam));
			beam.register(new BTBUsersService(beam));
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
			userReadLimit = new RateLimit("user-read", 500, 60);
		}

		return userReadLimit;
	}

	private static RateLimit getChannelReadLimit() {
		if (null == channelReadLimit) {
			channelReadLimit = new RateLimit("channel-read", 1000, 300);
		}

		return channelReadLimit;
	}

	public static BTBBeamUser getUser(int id) {
		BTBBeamUser user = null;

		try {
			user = getBeam().use(BTBUsersService.class).findOne(id).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return user;
	}

	public static BTBBeamUser getUser(String name) {
		BTBBeamUser user = null;

		try {
			user = getBeam().use(BTBUsersService.class).search(name).get().get(0);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return user;
	}

	public static BeamTeam getTeam(String token) {
		BeamTeam team = null;

		try {
			team = getBeam().use(TeamsService.class).findOne(token).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return team;
	}

	public static BeamTeam getTeam(int id) {
		BeamTeam team = null;

		try {
			team = getBeam().use(TeamsService.class).findOne(id).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return team;
	}

	public static List<BeamTeamUser> getTeamMembers(String team) {
		return getTeamMembers(getTeam(team));
	}

	public static List<BeamTeamUser> getTeamMembers(int id) {
		return getTeamMembers(getTeam(id));
	}

	public static List<BeamTeamUser> getTeamMembers(BeamTeam team) {
		TeamUserSearchResponse teamMembers = new TeamUserSearchResponse();
		int page = 0;

		try {
			while (teamMembers.addAll(getBeam().use(TeamsService.class).teamMembersOf(team, page++, 50).get()));
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return teamMembers;
	}

	public static List<BTBBeamChannel> getFollowing(int id) {
		return getFollowing(getUser(id));
	}

	public static List<BTBBeamChannel> getFollowing(BeamTeamUser user) {
		return getFollowing(getUser(user.id));
	}

	public static List<BTBBeamChannel> getFollowing(BTBBeamUser user) {
		BTBUserFollowsResponse following = new BTBUserFollowsResponse();

		int page = 0;

		try {
			while (getUserReadLimit().isNotLimited()
					&& following.addAll(getBeam().use(BTBUsersService.class).following(user.id, page++, 50).get()));
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return following;
	}

	public static BTBBeamChannel getChannel(int id) {
		BTBBeamChannel channel = null;

		try {
			for (int retry = 0; channel == null && retry < 10; ++retry) {
				if (getChannelReadLimit().isNotLimited()) {
					channel = getBeam().use(BTBChannelsService.class).findOne(id).get();
				}

				if (channel == null) {
					TimeUnit.SECONDS.sleep(1);
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return channel;
	}

	public static BTBBeamChannel getChannel(String name) {
		BTBBeamChannel channel = null;

		try {
			for (int retry = 0; channel == null && retry < 10; ++retry) {
				if (getChannelReadLimit().isNotLimited()) {
					channel = getBeam().use(BTBChannelsService.class).findOneByToken(name).get();
				}

				if (channel == null) {
					TimeUnit.SECONDS.sleep(1);
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return channel;
	}

	public static List<TeamMembershipExpanded> getTeams(BTBBeamUser user) {
		return getTeams(user.id);
	}

	public static List<TeamMembershipExpanded> getTeams(int id) {
		List<TeamMembershipExpanded> teams = new ArrayList<>();

		try {
			teams = getBeam().use(BTBUsersService.class).teams(id).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return teams;
	}
}
