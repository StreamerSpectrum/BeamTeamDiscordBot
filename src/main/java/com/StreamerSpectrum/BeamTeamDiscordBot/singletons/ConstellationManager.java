package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.TeamMembershipExpanded;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.google.gson.JsonObject;

import pro.beam.api.resource.constellation.events.EventHandler;
import pro.beam.api.resource.constellation.events.LiveEvent;
import pro.beam.api.resource.constellation.methods.LiveSubscribeMethod;
import pro.beam.api.resource.constellation.methods.LiveUnsubscribeMethod;
import pro.beam.api.resource.constellation.methods.data.LiveRequestData;
import pro.beam.api.resource.constellation.replies.LiveRequestReply;
import pro.beam.api.resource.constellation.replies.ReplyHandler;
import pro.beam.api.resource.constellation.ws.BeamConstellationConnectable;

public abstract class ConstellationManager {

	private static BeamConstellationConnectable connectable;

	private static void init() throws SQLException {
		handleEvents();

		subscribeToAnnouncements();

		List<BeamTeam> teams = DbManager.readAllTeams();

		for (BeamTeam team : teams) {
			subscribeToTeam(team);
		}

		List<BTBBeamChannel> channels = DbManager.readAllChannels();

		for (BTBBeamChannel channel : channels) {
			subscribeToChannel(channel.id);
		}

		// TODO: subscribe to tracked followers
		// TODO: subscribe to tracked followees
	}

	public static BeamConstellationConnectable getConnectable() {
		if (null == connectable || (null != connectable && connectable.isClosed())) {
			connectable = BeamManager.getConstellation().connectable(BeamManager.getBeam());

			connectable.connect();

			try {
				init();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return connectable;
	}

	public static void restartConstellation() {
		getConnectable().disconnect();

		for (int i = 0; i < 5 && getConnectable().isClosed(); ++i) {
			getConnectable().disconnect();
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void handleEvents() {
		getConnectable().on(LiveEvent.class, new EventHandler<LiveEvent>() {

			@Override
			public void onEvent(LiveEvent event) {
				switch (getEventFromEvent(event.data.channel)) {
					case "announcement:announce": {
						handleAnnouncements(event);
					}
					break;
					case "channel:update": {
						handleChannelUpdate(event);
					}
					break;
					case "channel:followed": {
						handleChannelFollowed(event);
					}
					break;
					case "team:memberAccepted": {
						handleTeamMemberAccepted(event);
					}
					break;
					case "team:memberInvited": {
						handleTeamMemberInvited(event);
					}
					break;
					case "team:memberRemoved": {
						handleTeamMemberRemoved(event);
					}
					break;
					case "team:ownerChanged": {
						handleTeamOwnerChanged(event);
					}
					break;
					case "user:followed": {
						handleUserFollowed(event);
					}
					break;
					default:
					break;
				}
			}
		});
	}

	private static void handleAnnouncements(LiveEvent event) {}

	private static void handleChannelUpdate(LiveEvent event) {
		try {
			JsonObject payload = event.data.payload;

			if (payload.has("online")) {
				BTBBeamChannel channel = BeamManager.getChannel(getIDFromEvent(event.data.channel));

				if (null != channel) {
					Set<BTBGuild> guilds = new HashSet<>();

					try {
						// Add all guilds that are tracking this
						// channel's teams to the announce set
						List<TeamMembershipExpanded> userTeams = BeamManager.getTeams(channel.userId);

						for (TeamMembershipExpanded team : userTeams) {
							guilds.addAll(DbManager.readGuildsForTrackedTeam(team.id, true));
						}

						// Add all guilds that are tracking this
						// channel
						// to the announce set
						guilds.addAll(DbManager.readGuildsForTrackedChannel(channel.id, true));

						// TODO: Check if this channel/channel's
						// user is
						// following a channel in a guild's tracked
						// follows
						// (get
						// all guilds in the db that are tracking
						// anyone
						// this channel/user is following)
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (!guilds.isEmpty()) {
						if (payload.get("online").getAsBoolean()) {
							System.out.println(String.format("%s is now live!", channel.user.username));

							for (BTBGuild guild : guilds) {
								String messageID = JDAManager.sendMessage(guild.getGoLiveChannelID(),
										JDAManager.buildGoLiveEmbed(channel));

								if (guild.isRemoveOfflineChannelAnnouncements()) {
									try {
										DbManager.createGoLiveMessage(messageID, guild.getGoLiveChannelID(),
												guild.getID(), channel.id);
									} catch (SQLException e) {
										// TODO Auto-generated catch
										// block
										e.printStackTrace();
									}
								}
							}
						} else {
							System.out.println(String.format("%s is now offline!", channel.user.username));
							// TODO: delete message when user goes
							// offline
							List<List<String>> messagesList = new ArrayList<List<String>>();

							try {
								messagesList = DbManager.readAllGoLiveMessagesForChannel(channel.id);

								for (List<String> values : messagesList) {
									JDAManager.deleteMessage(values.get(0), values.get(1), values.get(2));
								}

								DbManager.deleteGoLiveMessagesForChannel(channel.id);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						unsubscribeFromEvent(event.data.channel);
					}
				} else {
					System.out.println(String.format("Unable to retrieve channel info for channel id %d",
							getIDFromEvent(event.data.channel)));
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void handleChannelFollowed(LiveEvent event) {}

	private static void handleTeamMemberAccepted(LiveEvent event) {}

	private static void handleTeamMemberInvited(LiveEvent event) {}

	private static void handleTeamMemberRemoved(LiveEvent event) {}

	private static void handleTeamOwnerChanged(LiveEvent event) {}

	private static void handleUserFollowed(LiveEvent event) {

	}

	private static void subscribeToAnnouncements() {
		subscribeToEvent("announcement:announce");
	}

	public static void subscribeToChannel(int channelID) {
		subscribeToEvent(String.format("channel:%d:update", channelID));
		subscribeToEvent(String.format("channel:%d:followed", channelID));
	}

	public static void subscribeToTeam(BeamTeam team) {
		List<BeamTeamUser> teamMembers = BeamManager.getTeamMembers(team);

		for (BeamTeamUser member : teamMembers) {
			subscribeToChannel(member.channel.id);
		}

		subscribeToEvent(String.format("team:%d:memberAccepted", team.id));
		subscribeToEvent(String.format("team:%d:memberInvited", team.id));
		subscribeToEvent(String.format("team:%d:memberRemoved", team.id));
		subscribeToEvent(String.format("team:%d:ownerChanged", team.id));
	}

	public static void subscribeToUser(int userID) {
		subscribeToEvent(String.format("user:%d:followed", userID));
	}

	private static void subscribeToEvent(String event) {
		LiveSubscribeMethod lsm = new LiveSubscribeMethod();

		lsm.params = new LiveRequestData();
		lsm.params.events = new ArrayList<>();
		lsm.params.events.add(event);

		getConnectable().send(lsm, new ReplyHandler<LiveRequestReply>() {

			@Override
			public void onSuccess(LiveRequestReply result) {
				System.out.println(String.format("Successfully subscribed to %s.", event));
			}
		});
	}

	private static void unsubscribeFromEvent(String event) {
		LiveUnsubscribeMethod lum = new LiveUnsubscribeMethod();

		lum.params = new LiveRequestData();
		lum.params.events = new ArrayList<>();
		lum.params.events.add(event);

		getConnectable().send(lum, new ReplyHandler<LiveRequestReply>() {

			@Override
			public void onSuccess(LiveRequestReply result) {
				System.out.println(String.format("Successfully unsubscribed from %s.", event));
			}
		});
	}

	private static int getIDFromEvent(String event) {
		return Integer.parseInt(event.substring(event.indexOf(":") + 1, event.lastIndexOf(":")));
	}

	private static String getEventFromEvent(String event) {
		return event.replaceAll(":[0-9]*:", ":");
	}
}
