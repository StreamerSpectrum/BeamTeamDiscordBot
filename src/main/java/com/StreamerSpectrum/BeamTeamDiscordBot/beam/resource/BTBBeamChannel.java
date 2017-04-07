package com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource;

import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;
import com.google.gson.annotations.SerializedName;
import pro.beam.api.resource.channel.BeamResource;
import pro.beam.api.resource.channel.CachedMessage;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class BTBBeamChannel implements Serializable {
	public int							id;
	public String						token;
	public boolean						online;
	public boolean						featured;
	public boolean						partnered;
	public boolean						transcodingEnabled;
	public boolean						suspended;
	public boolean						interactive;
	public boolean						hasVod;
	public String						name;
	public AudienceRating				audience;
	public String						streamKey;
	public int							viewersTotal;
	public int							viewersCurrent;
	public int							numFollowers;
	public int							numSubscribers;
	public String						description;
	public int							typeId;
	public Date							createdAt;
	public Date							updatedAt;
	public int							userId;
	public int							coverId;
	public int							thumbnailId;
	public int							badgeId;
	public int							interactiveGameId;
	public BeamResource					thumbnail;
	public BeamResource					cover;
	public BeamResource					badge;
	public Type							type;
	public Map<String, Object>			preferences;
	@Deprecated
	@SerializedName("cache")
	public ArrayDeque<CachedMessage>	messageCache;
	public BTBBeamUser					user;

	public static class Type implements Serializable {
		public int		id;
		public String	name;
		public String	parent;
		public String	description;
		public String	source;
		public int		viewersCurrent;
		public int		online;
		public String	coverUrl;
	}

	public static enum CostreamPreference {
		@SerializedName("all")
		ALL, @SerializedName("following")
		FOLLOWING, @SerializedName("none")
		NONE
	}

	public static enum AudienceRating {
		@SerializedName("family")
		FAMILY, @SerializedName("teen")
		TEEN, @SerializedName("18+")
		ADULT
	}

	@Override
	public int hashCode() {
		return new Integer(id).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BTBBeamChannel && ((BTBBeamChannel) obj).id == this.id;
	}

	public Map<String, Object> getDbValues() {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.CHANNELS_COL_ID, id);
		values.put(Constants.CHANNELS_COL_NAME, user.username);
		values.put(Constants.CHANNELS_COL_TOKEN, token);
		values.put(Constants.CHANNELS_COL_USERID, user.id);

		return values;
	}
}
