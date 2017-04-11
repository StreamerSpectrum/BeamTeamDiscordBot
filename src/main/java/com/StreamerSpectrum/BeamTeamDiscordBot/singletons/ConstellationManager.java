package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.BTBMain;
import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.TeamMembershipExpanded;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.GoLiveMessage;
import com.google.gson.JsonObject;

import net.dv8tion.jda.core.EmbedBuilder;
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

		List<GoLiveMessage> messages = DbManager.readAllGoLiveMessages();

		for (GoLiveMessage message : messages) {
			JDAManager.deleteMessage(message);
		}

		DbManager.deleteAllGoLiveMessages();

		startupAnnounce();

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

	private static void startupAnnounce() {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.GUILDS_COL_GOLIVECHANNELID, "IS NOT NULL");

		List<BTBGuild> guilds = DbManager.readAllGuilds(where);

		for (BTBGuild guild : guilds) {
			Set<BTBBeamChannel> alreadyAnnounced = new HashSet<>();
			List<BeamTeam> teams = guild.getTrackedTeams();

			for (BeamTeam team : teams) {
				List<BeamTeamUser> members = BeamManager.getTeamMembers(team);

				for (BeamTeamUser member : members) {
					if (member.channel.online && !alreadyAnnounced.contains(member.channel)) {
						BTBBeamChannel channel = BeamManager.getChannel(member.channel.id);

						guild.sendGoLiveMessage(channel);

						alreadyAnnounced.add(channel);
					}
				}
			}

			List<BTBBeamChannel> channels = guild.getTrackedChannels();

			for (BTBBeamChannel channel : channels) {
				if (!alreadyAnnounced.contains(channel)) {
					channel = BeamManager.getChannel(channel.id);

					if (channel.online) {
						guild.sendGoLiveMessage(channel);

						alreadyAnnounced.add(channel);
					}
				}
			}
		}
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
					guilds.addAll(DbManager.readGuildsForTrackedTeam(team.id, true, false, false));
					guilds.addAll(DbManager.readGuildsForTrackedTeam(team.id, false, true, false));
				}

				guilds.addAll(DbManager.readGuildsForTrackedChannel(channel.id, true, false, false));
				guilds.addAll(DbManager.readGuildsForTrackedChannel(channel.id, false, true, false));

				if (!guilds.isEmpty()) {
					if (payload.get("online").getAsBoolean()) {
						for (BTBGuild guild : guilds) {
							guild.sendGoLiveMessage(channel);
							guild.sendLogMessage(String.format("**%s** has gone ***live***!", channel.user.username));
						}
					} else {
						List<GoLiveMessage> messagesList = new ArrayList<>();

						messagesList = DbManager.readAllGoLiveMessagesForChannel(channel.id);

						for (GoLiveMessage message : messagesList) {
							JDAManager.deleteMessage(message);
						}

						DbManager.deleteGoLiveMessagesForChannel(channel.id);

						for (BTBGuild guild : guilds) {
							guild.sendLogMessage(
									String.format("**%s** has gone ***offline***.", channel.user.username));
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
		Set<BTBGuild> guilds = new HashSet<BTBGuild>();
		guilds.addAll(DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true, false));
		guilds.addAll(DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, false, true));

		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			guild.sendLogMessage(
					String.format("**%s** has joined ***%s***!", payload.get("username").getAsString(), team.token));

			if (StringUtils.isNotBlank(guild.getNewMemberChannelID())) {
				BTBBeamUser member = BeamManager.getUser(payload.get("id").getAsInt());

				JDAManager.sendMessage(guild.getNewMemberChannelID(),
						"%s, please give a warm welcome to %s's newest member, %s!",
						JDAManager.getJDA().getGuildById(Long.toString(guild.getID())).getPublicRole().getAsMention(),
						team.name, member.username);
				JDAManager.sendMessage(guild.getNewMemberChannelID(), new EmbedBuilder()
						.setTitle(member.username, String.format("https://beam.pro/%s", member.username))
						.setThumbnail(String.format("https://beam.pro/api/v1/users/%d/avatar?_=%d", member.id,
								new Random().nextInt()))
						.setDescription(StringUtils.isBlank(member.bio) ? "No bio" : member.bio)
						.addField("Followers", Integer.toString(member.channel.numFollowers), true)
						.addField("Views", Integer.toString(member.channel.viewersTotal), true)
						.addField("Partnered", member.channel.partnered ? "Yes" : "No", true)
						.addField("Joined Beam", member.createdAt.toString(), true)
						.setImage(String.format("https://thumbs.beam.pro/channel/%d.small.jpg?_=%d", member.channel.id,
								new Random().nextInt()))
						.setFooter("Beam.pro", CommandHelper.BEAM_LOGO_URL).setTimestamp(Instant.now())
						.setColor(CommandHelper.COLOR).build());
			}
		}

		if (payload.has("social") && payload.get("social").getAsJsonObject().has("discord")) {
			JDAManager.giveTeamRoleToUserOnAllGuilds(getIDFromEvent(event.data.channel), JDAManager
					.getUserForDiscordTag(payload.get("social").getAsJsonObject().get("discord").getAsString()));
		}

		subscribeToChannel(BeamManager.getUser(payload.get("id").getAsInt()).channel);
	}

	private static void handleTeamMemberInvited(LiveEvent event, JsonObject payload) {
		List<BTBGuild> guilds = DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true,
				false);
		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			guild.sendLogMessage(String.format("**%s** has been invited to join ***%s***!",
					payload.get("username").getAsString(), team.token));
		}
	}

	private static void handleTeamMemberRemoved(LiveEvent event, JsonObject payload) {
		List<BTBGuild> guilds = DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true,
				false);
		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			guild.sendLogMessage(
					String.format("**%s** has left ***%s***.", payload.get("username").getAsString(), team.name));
		}

		if (payload.has("social") && payload.get("social").getAsJsonObject().has("discord")) {
			JDAManager.removeTeamRoleFromUserOnAllGuilds(getIDFromEvent(event.data.channel), JDAManager
					.getUserForDiscordTag(payload.get("social").getAsJsonObject().get("discord").getAsString()));

		}
	}

	private static void handleTeamOwnerChanged(LiveEvent event, JsonObject payload) {
		List<BTBGuild> guilds = DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true,
				false);
		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			guild.sendLogMessage(String.format("**%s** owner changed to ***%s***.", team.name,
					payload.get("username").getAsString()));
		}
	}

	private static void handleTeamDeleted(LiveEvent event, JsonObject payload) {
		List<BTBGuild> guilds = DbManager.readGuildsForTrackedTeam(getIDFromEvent(event.data.channel), false, true,
				false);
		BeamTeam team = DbManager.readTeam(getIDFromEvent(event.data.channel));

		for (BTBGuild guild : guilds) {
			guild.sendLogMessage(String.format("**%s** has been deleted from Beam.", team.name));
		}

		DbManager.deleteTeam(team.id);

		unsubscribeFromTeam(team);
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

	public static void unsubscribeFromTeam(BeamTeam team) {
		unsubscribeFromEvent(String.format("team:%d:memberAccepted", team.id));
		unsubscribeFromEvent(String.format("team:%d:memberInvited", team.id));
		unsubscribeFromEvent(String.format("team:%d:memberRemoved", team.id));
		unsubscribeFromEvent(String.format("team:%d:ownerChanged", team.id));
		unsubscribeFromEvent(String.format("team:%d:deleted", team.id));
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
