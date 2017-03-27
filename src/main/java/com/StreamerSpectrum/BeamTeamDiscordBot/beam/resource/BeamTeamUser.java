package com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public class BeamTeamUser implements Serializable {
	public Integer level;
	public Social social;
	public Integer id;
	public String username;
	public boolean verified;
	public Integer experience;
	public Integer sparks;
	public String avatarUrl;
	public String bio;
	public Integer primaryTeam;
	public Date createdAt;
	public Date updatedAt;
	public Channel channel;
	
	@SerializedName("TeamMembership")
	public TeamMembership teamMembership;

	public class Channel implements Serializable {
		public boolean featured;
		public Integer id;
		public Integer userId;
		public String token;
		public boolean online;
		public boolean partnered;
		public Integer transcodingProfileId;
		public boolean suspended;
		public String name;
		public String audience;
		public Integer viewersTotal;
		public Integer viewersCurrent;
		public Integer numFollowers;
		public String description;
		public Integer typeId;
		public boolean interactive;
		public Integer interactiveGameId;
		public Integer ftl;
		public boolean hasVod;
		public Integer languageId;
		public Integer coverId;
		public Integer thumbnailId;
		public Integer badgeId;
		public Integer hosteeId;
		public boolean hasTranscodes;
		public boolean vodsEnabled;
		public Date createdAt;
		public Date updatedAt;
	}

	public class TeamMembership implements Serializable {
		public Integer teamId;
		public Integer userId;
		public boolean accepted;
		public Date createdAt;
		public Date updatedAt;
	}

	public class Social implements Serializable {
		public String facebook;
		public String twitter;
		public String youtube;
		public String discord;
		public String player;
	}
}