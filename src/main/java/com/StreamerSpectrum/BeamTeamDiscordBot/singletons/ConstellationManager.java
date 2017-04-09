package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.TeamMembershipExpanded;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.GoLiveMessage;
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

		List<BeamTeam> teams = DbManager.readAllTrackedTeams();

		for (BeamTeam team : teams) {
			subscribeToTeam(team);
		}

		List<BTBBeamChannel> channels = DbManager.readAllChannels();

		for (BTBBeamChannel channel : channels) {
			subscribeToChannel(channel);
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
						handleAnnouncements(event, event.data.payload);
					}
					break;
					case "channel:update": {
						handleChannelUpdate(event, event.data.payload);
					}
					break;
					case "channel:followed": {
						handleChannelFollowed(event, event.data.payload);
					}
					break;
					case "team:memberAccepted": {
						handleTeamMemberAccepted(event, event.data.payload);
					}
					break;
					case "team:memberInvited": {
						handleTeamMemberInvited(event, event.data.payload);
					}
					break;
					case "team:memberRemoved": {
						handleTeamMemberRemoved(event, event.data.payload);
					}
					break;
					case "team:ownerChanged": {
						handleTeamOwnerChanged(event, event.data.payload);
					}
					break;
					case "team:deleted": {
						handleTeamDeleted(event, event.data.payload);
					}
					break;
					case "user:followed": {
						handleUserFollowed(event, event.data.payload);
					}
					break;
					default:
						System.out.println(String.format("Unknown event: %s\n%s", event.data.channel,
								event.data.payload.toString()));
						try {
							if (Files.notExists(Paths.get("payloads\\"))) {
								new File("payloads\\").mkdir();
							}

							Logger logger = Logger.getLogger(String.format("payload-%s",
									getEventFromEvent(event.data.channel).replaceAll(":", "")));
							FileHandler fh = new FileHandler("payloads\\" + logger.getName() + ".json");
							SimpleFormatter formatter = new SimpleFormatter();
							fh.setFormatter(formatter);

							logger.addHandler(fh);

							logger.log(Level.INFO, event.data.payload.toString());
						} catch (SecurityException | IOException e) {}
					break;
				}
			}
		});
	}

	private static void handleAnnouncements(LiveEvent event, JsonObject payload) {
		try {
			if (Files.notExists(Paths.get("payloads\\"))) {
				new File("payloads\\").mkdir();
			}

			Logger logger = Logger.getLogger("payload-announcement");
			FileHandler fh = new FileHandler("payloads\\" + logger.getName() + ".json");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			logger.addHandler(fh);

			logger.log(Level.INFO, payload.toString());
		} catch (SecurityException | IOException e) {}
	}

	private static void handleChannelUpdate(LiveEvent event, JsonObject payload) {
		try {
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
								try {
									DbManager.createGoLiveMessage(new GoLiveMessage(messageID,
											guild.getGoLiveChannelID(), guild.getID(), channel.id));
								} catch (SQLException e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
								}
							}
						} else {
							System.out.println(String.format("%s is now offline!", channel.user.username));
							// TODO: delete message when user goes
							// offline
							List<GoLiveMessage> messagesList = new ArrayList<>();

							try {
								messagesList = DbManager.readAllGoLiveMessagesForChannel(channel.id);

								for (GoLiveMessage message : messagesList) {
									JDAManager.deleteMessage(message);
								}

								DbManager.deleteGoLiveMessagesForChannel(channel.id);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						System.out.println(String.format("No one is tracking %s's channel.", channel.user.username));
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

	private static void handleChannelFollowed(LiveEvent event, JsonObject payload) {
		try {
			if (Files.notExists(Paths.get("payloads\\"))) {
				new File("payloads\\").mkdir();
			}

			Logger logger = Logger.getLogger("payload-channelFollowed");
			FileHandler fh = new FileHandler("payloads\\" + logger.getName() + ".json");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			logger.addHandler(fh);

			logger.log(Level.INFO, payload.toString());
		} catch (SecurityException | IOException e) {}
	}

	private static void handleTeamMemberAccepted(LiveEvent event, JsonObject payload) {
		// TODO: Make a "member accepted" announcement to the appropriate
		// channels
		subscribeToChannel(BeamManager.getUser(payload.get("id").getAsInt()).channel);
	}

	private static void handleTeamMemberInvited(LiveEvent event, JsonObject payload) {
		try {
			if (Files.notExists(Paths.get("payloads\\"))) {
				new File("payloads\\").mkdir();
			}

			Logger logger = Logger.getLogger("payload-teamMemberInvited");
			FileHandler fh = new FileHandler("payloads\\" + logger.getName() + ".json");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			logger.addHandler(fh);

			logger.log(Level.INFO, payload.toString());
		} catch (SecurityException | IOException e) {}
		// TODO: Make a "member was invited" announcement to the appropriate
		// channels
	}

	private static void handleTeamMemberRemoved(LiveEvent event, JsonObject payload) {
		try {
			if (Files.notExists(Paths.get("payloads\\"))) {
				new File("payloads\\").mkdir();
			}

			Logger logger = Logger.getLogger("payload-teamMemberRemoved");
			FileHandler fh = new FileHandler("payloads\\" + logger.getName() + ".json");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			logger.addHandler(fh);

			logger.log(Level.INFO, payload.toString());
		} catch (SecurityException | IOException e) {}
		// TODO: Make a "member has left" announcement to the appropriate
		// channels
	}

	private static void handleTeamOwnerChanged(LiveEvent event, JsonObject payload) {
		try {
			if (Files.notExists(Paths.get("payloads\\"))) {
				new File("payloads\\").mkdir();
			}

			Logger logger = Logger.getLogger("payload-teamOwnerChanged");
			FileHandler fh = new FileHandler("payloads\\" + logger.getName() + ".json");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			logger.addHandler(fh);

			logger.log(Level.INFO, payload.toString());
		} catch (SecurityException | IOException e) {}
	}

	private static void handleTeamDeleted(LiveEvent event, JsonObject payload) {
		try {
			if (Files.notExists(Paths.get("payloads\\"))) {
				new File("payloads\\").mkdir();
			}

			Logger logger = Logger.getLogger("payload-teamDeleted");
			FileHandler fh = new FileHandler("payloads\\" + logger.getName() + ".json");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			logger.addHandler(fh);

			logger.log(Level.INFO, payload.toString());
		} catch (SecurityException | IOException e) {}
	}

	private static void handleUserFollowed(LiveEvent event, JsonObject payload) {
		try {
			if (Files.notExists(Paths.get("payloads\\"))) {
				new File("payloads\\").mkdir();
			}

			Logger logger = Logger.getLogger("payload-userFollowed");
			FileHandler fh = new FileHandler("payloads\\" + logger.getName() + ".json");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			logger.addHandler(fh);

			logger.log(Level.INFO, payload.toString());
		} catch (SecurityException | IOException e) {}
	}

	private static void subscribeToAnnouncements() {
		subscribeToEvent("announcement:announce");
	}

	public static void subscribeToChannel(BTBBeamChannel channel) {
		System.out.println(String.format("Subscribing to %s's channel.", channel.token));

		subscribeToEvent(String.format("channel:%d:update", channel.id));
		subscribeToEvent(String.format("channel:%d:status", channel.id));
		subscribeToEvent(String.format("chat:%d:StartStreaming", channel.id));
		subscribeToEvent(String.format("chat:%d:StopStreaming", channel.id));
	}

	public static void subscribeToChannelFollowers(int channelID) {
		subscribeToEvent(String.format("channel:%d:followed", channelID));
	}

	public static void subscribeToTeam(BeamTeam team) {
		System.out.println(String.format("Subscribing to team \"%s\" and its members' channels.", team.name));
		List<BeamTeamUser> teamMembers = BeamManager.getTeamMembers(team);

		for (BeamTeamUser member : teamMembers) {
			subscribeToChannel(member.channel);
		}

		subscribeToEvent(String.format("team:%d:memberAccepted", team.id));
		subscribeToEvent(String.format("team:%d:memberInvited", team.id));
		subscribeToEvent(String.format("team:%d:memberRemoved", team.id));
		subscribeToEvent(String.format("team:%d:ownerChanged", team.id));
		subscribeToEvent(String.format("team:%d:deleted", team.id));
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
			public void onSuccess(LiveRequestReply result) {}
		});
		System.out.println(String.format("Successfully subscribed to %s", event));
	}

	private static void unsubscribeFromEvent(String event) {
		LiveUnsubscribeMethod lum = new LiveUnsubscribeMethod();

		lum.params = new LiveRequestData();
		lum.params.events = new ArrayList<>();
		lum.params.events.add(event);

		getConnectable().send(lum, new ReplyHandler<LiveRequestReply>() {

			@Override
			public void onSuccess(LiveRequestReply result) {}
		});
		System.out.println(String.format("Successfully unsubscribed from %s", event));
	}

	private static int getIDFromEvent(String event) {
		return Integer.parseInt(event.substring(event.indexOf(":") + 1, event.lastIndexOf(":")));
	}

	private static String getEventFromEvent(String event) {
		return event.replaceAll(":[0-9]*:", ":");
	}
}
