package com.StreamerSpectrum.BeamTeamDiscordBot;

public abstract class Constants {
	// Database Tables
	public static final String	TABLE_CHANNELS									= "Channels";
	public static final String	TABLE_GOLIVEMESSAGES							= "GoLiveMessages";
	public static final String	TABLE_GUILDS									= "Guilds";
	public static final String	TABLE_TEAMS										= "Teams";
	public static final String	TABLE_TRACKEDCHANNELS							= "TrackedChannels";
	public static final String	TABLE_TRACKEDTEAMS								= "TrackedTeams";

	// Database Columns
	public static final String	CHANNELS_COL_ID									= "ID";
	public static final String	CHANNELS_COL_NAME								= "Name";
	public static final String	CHANNELS_COL_TOKEN								= "Token";
	public static final String	CHANNELS_COL_USERID								= "UserID";

	public static final String	GOLIVEMESSAGES_COL_ID							= "ID";
	public static final String	GOLIVEMESSAGES_COL_GOLIVECHANNELID				= "GoLiveChannelID";
	public static final String	GOLIVEMESSAGES_COL_GUILDID						= "GuildID";
	public static final String	GOLIVEMESSAGES_COL_BEAMCHANNELID				= "BeamChannelID";

	public static final String	GUILDS_COL_ID									= "ID";
	public static final String	GUILDS_COL_NAME									= "Name";
	public static final String	GUILDS_COL_GOLIVECHANNELID						= "GoLiveChannelID";
	public static final String	GUILDS_COL_LOGCHANNELID							= "LogChannelID";
	public static final String	GUILDS_COL_REMOVEOFFLINECHANNELANNOUNCEMENTS	= "RemoveOfflineChannels";

	public static final String	TEAMS_COL_ID									= "ID";
	public static final String	TEAMS_COL_NAME									= "Name";
	public static final String	TEAMS_COL_TOKEN									= "Token";

	public static final String	TRACKEDCHANNELS_COL_GUILDID						= "GuildID";
	public static final String	TRACKEDCHANNELS_COL_BEAMCHANNELID				= "BeamChannelID";

	public static final String	TRACKEDTEAMS_COL_GUILDID						= "GuildID";
	public static final String	TRACKEDTEAMS_COL_TEAMID							= "TeamID";
}
