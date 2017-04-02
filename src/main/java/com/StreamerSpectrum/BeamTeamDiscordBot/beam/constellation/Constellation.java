package com.StreamerSpectrum.BeamTeamDiscordBot.beam.constellation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.Guild;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.BeamManager;
import com.StreamerSpectrum.BeamTeamDiscordBot.singletons.JDAManager;
import com.google.gson.JsonObject;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pro.beam.api.resource.channel.BeamChannel;
import pro.beam.api.resource.constellation.BeamConstellation;
import pro.beam.api.resource.constellation.events.LiveEvent;
import pro.beam.api.resource.constellation.methods.LiveSubscribeMethod;
import pro.beam.api.resource.constellation.methods.LiveUnsubscribeMethod;
import pro.beam.api.resource.constellation.methods.data.LiveRequestData;
import pro.beam.api.resource.constellation.replies.LiveRequestReply;
import pro.beam.api.resource.constellation.replies.ReplyHandler;
import pro.beam.api.resource.constellation.ws.BeamConstellationConnectable;
import pro.beam.api.resource.constellation.events.EventHandler;

public class Constellation {

	private static final long				CONNECTION_CHECK_INTERVAL	= 15 * 60 * 1000;	// 15
	// minutes

	private final BeamConstellation				constellation;
	private final BeamConstellationConnectable	connectable;

	private final Guild						owner;

	private Timer							connectionCheckTimer;

	public Constellation(Guild owner) {
		this.owner = owner;

		constellation = new BeamConstellation();
		connectable = new BeamConstellationConnectable(BeamManager.getBeam(), constellation);

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

	private void handleChannelLive() {
		connectable.on(LiveEvent.class, new EventHandler<LiveEvent>() {

			@Override
			public void onEvent(LiveEvent event) {
				try {
					JsonObject payload = event.data.payload;

					if (payload.has("online")) {
						BeamChannel channel = BeamManager.getChannel(getIDFromEvent(event.data.channel));

						if (null != channel) {
							System.out.println(String.format("%s's stream is %s", channel.user.username, payload.get("online").getAsBoolean() ? "live" : "offline"));
							
							if (payload.get("online").getAsBoolean()) {
								MessageEmbed embed = new EmbedBuilder()
										.setTitle(String.format("%s is now live!", channel.user.username),
												String.format("https://beam.pro/%s", channel.user.username))
										.setThumbnail(String.format("https://beam.pro/api/v1/users/%d/avatar?w=64&h=64",
												channel.user.id))
										.setDescription(StringUtils.isBlank(channel.user.bio) ? "No bio" : channel.user.bio)
										.addField(channel.name, channel.type.name, false)
										.addField("Followers", Integer.toString(channel.numFollowers), true)
										.addField("Views", Integer.toString(channel.viewersTotal), true)
										.addField("Rating", channel.audience.toString(), true)
										.setImage(String.format("https://thumbs.beam.pro/channel/%d.small.jpg?_%d", channel.id, new Random().nextInt()))
										.setFooter("Beam.pro", CommandHelper.BEAM_LOGO_URL).setTimestamp(Instant.now())
										.setColor(CommandHelper.COLOR).build();
	
								JDAManager.sendMessage(owner.getOptions().getGoLiveChannelID(), embed);
							} else {
								// TODO: delete message when user goes offline
							}
						} else {
							System.out.println(String.format("Unable to retrieve channel info for channel id %d", getIDFromEvent(event.data.channel)));
						}
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
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
}
