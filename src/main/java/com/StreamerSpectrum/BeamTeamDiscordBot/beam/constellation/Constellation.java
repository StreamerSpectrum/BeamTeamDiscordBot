package com.StreamerSpectrum.BeamTeamDiscordBot.beam.constellation;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.GuildManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;
import com.google.gson.JsonObject;

import pro.beam.api.resource.constellation.events.EventHandler;
import pro.beam.api.resource.constellation.events.LiveEvent;
import pro.beam.api.resource.constellation.methods.LiveSubscribeMethod;
import pro.beam.api.resource.constellation.methods.LiveUnsubscribeMethod;
import pro.beam.api.resource.constellation.methods.data.LiveRequestData;
import pro.beam.api.resource.constellation.replies.LiveRequestReply;
import pro.beam.api.resource.constellation.replies.ReplyHandler;
import pro.beam.api.resource.constellation.ws.BeamConstellationConnectable;

public class Constellation {

	private static final long					CONNECTION_CHECK_INTERVAL	= 15 * 60 * 1000;	// 15
	// minutes

	private final BeamConstellationConnectable	connectable;

	private final long							guildID;

	private Timer								connectionCheckTimer;

	public Constellation(long guildID) {
		this.guildID = guildID;

		connectable = BeamManager.getConstellation().connectable(BeamManager.getBeam());

		connectable.connect();

		connectionCheckTimer = new Timer();
		connectionCheckTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (connectable.isClosed()) {
					// TODO: let the bot/log channel know that constellation
					// disconnected and a retry attempt is about to happen

					for (int retry = 0; connectable.isClosed() && retry < 10; ++retry) {
						connectable.connect();

						try {
							TimeUnit.SECONDS.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					if (connectable.isClosed()) {
						// TODO: let the bot/log channel know that constellation
						// is still disconnected and another retry will happen
						// in 15 minutes

					}
				}
			}
		}, CONNECTION_CHECK_INTERVAL, CONNECTION_CHECK_INTERVAL);

		handleChannelLive();
	}

	private BTBGuild getGuild() {
		return GuildManager.getGuild(guildID);
	}

	private void handleChannelLive() {
		connectable.on(LiveEvent.class, new EventHandler<LiveEvent>() {

			@Override
			public void onEvent(LiveEvent event) {
				try {
					JsonObject payload = event.data.payload;

					if (payload.has("online")) {
						BTBBeamChannel channel = BeamManager.getChannel(getIDFromEvent(event.data.channel));

						if (null != channel) {
							System.out.println(String.format("%s's stream is %s", channel.user.username,
									payload.get("online").getAsBoolean() ? "live" : "offline"));

							if (payload.get("online").getAsBoolean()) {
								JDAManager.sendMessage(getGuild().getGoLiveChannelID(),
										JDAManager.buildGoLiveEmbed(channel));
							} else {
								// TODO: delete message when user goes offline
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

		});

	}

	public void subscribeToChannel(int channelID) {
		subscribeToEvent(String.format("channel:%d:update", channelID));
	}

	public void unsubscribeFromChannel(int channelID) {
		unsubscribeFromEvent(String.format("channel:%d:update", channelID));
	}

	private void subscribeToEvent(String event) {
		LiveSubscribeMethod lsm = new LiveSubscribeMethod();

		lsm.params = new LiveRequestData();
		lsm.params.events = new ArrayList<>();
		lsm.params.events.add(event);

		connectable.send(lsm, new ReplyHandler<LiveRequestReply>() {

			@Override
			public void onSuccess(LiveRequestReply result) {
				System.out.println(String.format("Successfully subscribed to %s.", event));
			}
		});
	}

	private void unsubscribeFromEvent(String event) {
		LiveUnsubscribeMethod lum = new LiveUnsubscribeMethod();

		lum.params = new LiveRequestData();
		lum.params.events = new ArrayList<>();
		lum.params.events.add(event);

		connectable.send(lum, new ReplyHandler<LiveRequestReply>() {

			@Override
			public void onSuccess(LiveRequestReply result) {
				System.out.println(String.format("Successfully unsubscribed from %s.", event));
			}
		});
	}

	private int getIDFromEvent(String event) {
		return Integer.parseInt(event.substring(event.indexOf(":") + 1, event.lastIndexOf(":")));
	}

	public void disconnect() {
		connectable.disconnect();
	}
}
