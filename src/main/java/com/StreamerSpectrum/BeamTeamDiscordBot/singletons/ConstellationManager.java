package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.BTBMain;
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
	private final static Logger					logger	= Logger.getLogger(BTBMain.class.getName());

	private static BeamConstellationConnectable	connectable;

	private static void init() {
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

			init();
		}

		return connectable;
	}

	public static void restartConstellation() {
		getConnectable().disconnect();

		for (int i = 0; i < 5 && getConnectable().isClosed(); ++i) {
			getConnectable().disconnect();
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException ignore) {}
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
						logger.log(Level.INFO, String.format("Unknown event: %s\n%s", event.data.channel,
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
		if (payload.has("online")) {
			BTBBeamChannel channel = BeamManager.getChannel(getIDFromEvent(event.data.channel));

			if (null != channel) {
				Set<BTBGuild> guilds = new HashSet<>();

				List<TeamMembershipExpanded> userTeams = BeamManager.getTeams(channel.userId);

				for (TeamMembershipExpanded team : userTeams) {
					guilds.addAll(DbManager.readGuildsForTrackedTeam(team.id, true, false));
					guilds.addAll(DbManager.readGuildsForTrackedTeam(team.id, false, true));
				}

				guilds.addAll(DbManager.readGuildsForTrackedChannel(channel.id, true, false));
				guilds.addAll(DbManager.readGuildsForTrackedChannel(channel.id, false, true));

				if (!guilds.isEmpty()) {
					if (payload.get("online").getAsBoolean()) {
						for (BTBGuild guild : guilds) {
							if (StringUtils.isNotBlank(guild.getGoLiveChannelID())) {
								JDAManager.sendGoLiveMessage(guild.getGoLiveChannelID(),
										JDAManager.buildGoLiveEmbed(channel), channel);
							}

							if (StringUtils.isNotBlank(guild.getLogChannelID())) {
								JDAManager.sendMessage(guild.getLogChannelID(), "**%s** has gone ***live***!",
										channel.user.username);
							}
						}
					} else {
						List<GoLiveMessage> messagesList = new ArrayList<>();

						messagesList = DbManager.readAllGoLiveMessagesForChannel(channel.id);

						for (GoLiveMessage message : messagesList) {
							if (DbManager.readGuild(message.getGuildID()).isRemoveOfflineChannelAnnouncements()) {
								JDAManager.deleteMessage(message);
							}
						}

						DbManager.deleteGoLiveMessagesForChannel(channel.id);

						for (BTBGuild guild : guilds) {
							if (StringUtils.isNotBlank(guild.getLogChannelID())) {
								JDAManager.sendMessage(guild.getLogChannelID(), "**%s** has gone ***offline***!",
										channel.user.username);
							}
						}
					}
				} else {
					logger.log(Level.INFO, String.format("No one is tracking %s's channel.", channel.user.username));
					unsubscribeFromEvent(event.data.channel);
				}
			} else {
				logger.log(Level.INFO, String.format("Unable to retrieve channel info for channel id %d",
						getIDFromEvent(event.data.channel)));
			}
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
		List<BTBGuild> guilds = DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true);
		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			JDAManager.sendMessage(guild.getLogChannelID(), "**%s** has joined ***%s***!",
					payload.get("username").getAsString(), team.token);
		}

		if (payload.has("social") && payload.get("social").getAsJsonObject().has("discord")) {
			JDAManager.giveTeamRoleToUserOnAllGuilds(getIDFromEvent(event.data.channel), JDAManager
					.getUserForDiscordTag(payload.get("social").getAsJsonObject().get("discord").getAsString()));
		}

		subscribeToChannel(BeamManager.getUser(payload.get("id").getAsInt()).channel);
	}

	private static void handleTeamMemberInvited(LiveEvent event, JsonObject payload) {
		List<BTBGuild> guilds = DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true);
		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			JDAManager.sendMessage(guild.getLogChannelID(), "**%s** has been invited to join ***%s***!",
					payload.get("username").getAsString(), team.token);
		}
		// TODO: Make a "member was invited" announcement to the appropriate
		// channels
	}

	private static void handleTeamMemberRemoved(LiveEvent event, JsonObject payload) {
		// TODO: Make a "member has left" announcement to the appropriate
		// channels
		List<BTBGuild> guilds = DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true);
		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			JDAManager.sendMessage(guild.getLogChannelID(), "**%s** has left ***%s***.",
					payload.get("username").getAsString(), team.name);
		}

		if (payload.has("social") && payload.get("social").getAsJsonObject().has("discord")) {
			JDAManager.removeTeamRoleFromUserOnAllGuilds(getIDFromEvent(event.data.channel), JDAManager
					.getUserForDiscordTag(payload.get("social").getAsJsonObject().get("discord").getAsString()));

		}
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
		List<BTBGuild> guilds = DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true);
		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			JDAManager.sendMessage(guild.getLogChannelID(), "**%s** has been deleted from Beam.", team.name);
		}

		DbManager.deleteTeam(team.id);
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
		logger.log(Level.INFO, String.format("Subscribing to %s's channel.", channel.token));

		subscribeToEvent(String.format("channel:%d:update", channel.id));
	}

	public static void subscribeToChannelFollowers(int channelID) {
		subscribeToEvent(String.format("channel:%d:followed", channelID));
	}

	public static void subscribeToTeam(BeamTeam team) {
		logger.log(Level.INFO, String.format("Subscribing to team \"%s\" and its members' channels.", team.name));

		subscribeToEvent(String.format("team:%d:memberAccepted", team.id));
		subscribeToEvent(String.format("team:%d:memberInvited", team.id));
		subscribeToEvent(String.format("team:%d:memberRemoved", team.id));
		subscribeToEvent(String.format("team:%d:ownerChanged", team.id));
		subscribeToEvent(String.format("team:%d:deleted", team.id));

		List<BeamTeamUser> teamMembers = BeamManager.getTeamMembers(team);

		for (BeamTeamUser member : teamMembers) {
			subscribeToChannel(member.channel);
		}
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
	}

	private static int getIDFromEvent(String event) {
		return Integer.parseInt(event.substring(event.indexOf(":") + 1, event.lastIndexOf(":")));
	}

	private static String getEventFromEvent(String event) {
		return event.replaceAll(":[0-9]*:", ":");
	}
}
