package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.GoLiveMessage;

public abstract class DbManager {

	private static Connection connection;

	private static Connection getConnection() {
		if (null == connection) {
			try {
				File dbFile = new File("resources/bt.db");
				if (!dbFile.exists()) {
					FileUtils.copyFile(new File("resources/bt.db.template"), dbFile);
				}

				updateDb();

				connection = DriverManager.getConnection("jdbc:sqlite:resources/bt.db");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return connection;
	}

	private static void updateDb() {}

	private static boolean create(String tableName, Map<String, Object> values) throws SQLException {
		if (StringUtils.isBlank(tableName)) {
			return false;
		}

		PreparedStatement statement = null;

		StringBuilder columns = new StringBuilder();
		StringBuilder vals = new StringBuilder();
		for (String key : values.keySet()) {
			columns.append(key).append(", ");
			vals.append("?, ");
		}

		try {
			statement = getConnection().prepareStatement(String.format("INSERT INTO %s (%s) VALUES (%s);", tableName,
					columns.substring(0, columns.lastIndexOf(",")), vals.substring(0, vals.lastIndexOf(","))));
			statement.setQueryTimeout(30);

			int i = 1;
			for (String key : values.keySet()) {
				statement.setObject(i++, values.get(key));
			}

			return statement.executeUpdate() > 0;
		} finally {
			if (null != statement) {
				statement.close();
			}
		}
	}

	private static List<List<String>> read(String tableName, String[] columns, String innerJoin, String where)
			throws SQLException {
		if (StringUtils.isBlank(tableName)) {
			return new ArrayList<>();
		}

		Statement statement = null;

		StringBuilder cols = new StringBuilder();
		if (null != columns && columns.length > 0) {

			for (String columnName : columns) {
				if (StringUtils.isNotBlank(columnName)) {
					cols.append(columnName).append(", ");
				}
			}
		}

		try {
			statement = getConnection().createStatement();
			statement.setQueryTimeout(30);

			StringBuilder sql = new StringBuilder();
			sql.append(String.format("SELECT %s FROM %s",
					cols.length() > 0 ? cols.substring(0, cols.lastIndexOf(",")) : "*", tableName));

			if (StringUtils.isNotBlank(innerJoin)) {
				sql.append(String.format(" INNER JOIN %s", innerJoin));
			}

			if (StringUtils.isNotBlank(where)) {
				sql.append(String.format(" WHERE %s", where));
			}

			sql.append(";");

			ResultSet rs = statement.executeQuery(sql.toString());
			List<List<String>> values = new ArrayList<>();

			while (rs.next()) {
				List<String> vals = new ArrayList<>();

				if (null != columns && columns.length > 0) {
					for (String columnName : columns) {
						vals.add(rs.getString(rs.findColumn(columnName)));
					}
				} else {
					int i = 1;

					while (true) {
						try {
							vals.add(rs.getString(i++));
						} catch (SQLException e) {
							break;
						}
					}
				}

				values.add(vals);
			}

			return values;
		} finally {
			if (null != statement) {
				statement.close();
			}
		}
	}

	private static boolean update(String tableName, Map<String, Object> newVals, String where) throws SQLException {
		if (StringUtils.isBlank(tableName)) {
			return false;
		}

		Statement statement = null;

		StringBuilder sets = new StringBuilder();
		for (String key : newVals.keySet()) {
			if (newVals.get(key) == null) {
				sets.append(String.format("%s = null, ", key));
			} else {
				sets.append(String.format("%s = '%s', ", key, newVals.get(key)));
			}
		}

		try {
			statement = getConnection().createStatement();
			statement.setQueryTimeout(30);

			StringBuilder sql = new StringBuilder();
			sql.append(String.format("UPDATE %s SET %s", tableName, sets.substring(0, sets.lastIndexOf(","))));

			if (StringUtils.isNotBlank(where)) {
				sql.append(String.format(" WHERE %s", where));
			}

			sql.append(";");

			return statement.executeUpdate(sql.toString()) > 0;
		} finally {
			if (null != statement) {
				statement.close();
			}
		}
	}

	private static boolean delete(String tableName, String where) throws SQLException {
		if (StringUtils.isBlank(tableName)) {
			return false;
		}

		Statement statement = null;

		try {
			statement = getConnection().createStatement();
			statement.setQueryTimeout(30);

			return statement.executeUpdate(String.format("DELETE FROM %s %s;", tableName,
					null == where ? ";" : String.format("WHERE %s", where))) > 0;

		} finally {
			if (null != statement) {
				statement.close();
			}
		}
	}

	public static boolean createGuild(BTBGuild guild) throws SQLException {
		return create(Constants.TABLE_GUILDS, guild.getDbValues());
	}

	public static BTBGuild readGuild(long id) throws SQLException {
		List<List<String>> values = read(Constants.TABLE_GUILDS, null, null,
				String.format("%s = %d", Constants.GUILDS_COL_ID, id));

		BTBGuild guild = null;

		if (!values.isEmpty()) {
			guild = new BTBGuild(id, GuildManager.getShardID(id), values.get(0).get(1), values.get(0).get(2),
					values.get(0).get(3), "1".equals((values.get(0).get(4))));
		}

		return guild;
	}

	public static List<BTBGuild> readAllGuilds() throws SQLException {
		List<BTBGuild> guilds = new ArrayList<BTBGuild>();
		List<List<String>> valuesList = read(Constants.TABLE_GUILDS, null, null, null);

		for (List<String> values : valuesList) {
			guilds.add(
					new BTBGuild(Long.parseLong(values.get(0)), GuildManager.getShardID(Long.parseLong(values.get(0))),
							values.get(1), values.get(2), values.get(3), "1".equals((values.get(4)))));
		}

		return guilds;
	}

	public static boolean updateGuild(BTBGuild guild) throws SQLException {
		return update(Constants.TABLE_GUILDS, guild.getDbValues(),
				String.format("%s = %d", Constants.GUILDS_COL_ID, guild.getID()));
	}

	public static boolean deleteGuild(long id) throws SQLException {
		return delete(Constants.TABLE_GUILDS, String.format("%s = %d", Constants.GUILDS_COL_ID, id));
	}

	public static boolean createChannel(BTBBeamChannel channel) throws SQLException {
		return create(Constants.TABLE_CHANNELS, channel.getDbValues());
	}

	public static BTBBeamChannel readChannel(int id) throws SQLException {
		List<List<String>> values = read(Constants.TABLE_CHANNELS, null, null,
				String.format("%s = %d", Constants.CHANNELS_COL_ID, id));

		BTBBeamChannel channel = null;

		if (!values.isEmpty()) {
			channel = new BTBBeamChannel();

			channel.id = Integer.parseInt(values.get(0).get(0));
			channel.user = new BTBBeamUser();
			channel.user.username = values.get(0).get(1);
			channel.token = values.get(0).get(2);
			channel.userId = Integer.parseInt(values.get(0).get(3));
		}

		return channel;
	}

	public static List<BTBBeamChannel> readAllChannels() throws SQLException {
		List<BTBBeamChannel> channels = new ArrayList<BTBBeamChannel>();
		List<List<String>> valuesList = read(Constants.TABLE_CHANNELS, null, null, null);

		for (List<String> values : valuesList) {
			BTBBeamChannel channel = new BTBBeamChannel();

			channel.id = Integer.parseInt(values.get(0));
			channel.user = new BTBBeamUser();
			channel.user.username = values.get(1);
			channel.token = values.get(2);
			channel.userId = Integer.parseInt(values.get(3));

			channels.add(channel);
		}

		return channels;
	}

	public static boolean updateChannel(BTBBeamChannel channel) throws SQLException {
		return update(Constants.TABLE_CHANNELS, channel.getDbValues(),
				String.format("%s = %d", Constants.CHANNELS_COL_ID, channel.id));
	}

	public static boolean deleteChannel(int id) throws SQLException {
		return delete(Constants.TABLE_CHANNELS, String.format("%s = %d", Constants.CHANNELS_COL_ID, id));
	}

	public static boolean createTeam(BeamTeam team) throws SQLException {
		return create(Constants.TABLE_TEAMS, team.getDbValues());
	}

	public static BeamTeam readTeam(int id) throws SQLException {
		List<List<String>> values = read(Constants.TABLE_TEAMS, null, null,
				String.format("%s = %d", Constants.TEAMS_COL_ID, id));

		BeamTeam team = null;

		if (!values.isEmpty()) {
			team = new BeamTeam();

			team.id = Integer.parseInt(values.get(0).get(0));
			team.name = values.get(0).get(1);
			team.token = values.get(0).get(2);
		}

		return team;
	}

	public static List<BeamTeam> readAllTeams() throws SQLException {
		List<BeamTeam> teams = new ArrayList<BeamTeam>();
		List<List<String>> valuesList = read(Constants.TABLE_TEAMS, null, null, null);

		for (List<String> values : valuesList) {
			BeamTeam team = new BeamTeam();

			team.id = Integer.parseInt(values.get(0));
			team.name = values.get(1);
			team.token = values.get(2);

			teams.add(team);
		}

		return teams;
	}

	public static boolean updateTeam(BeamTeam team) throws SQLException {
		return update(Constants.TABLE_TEAMS, team.getDbValues(),
				String.format("%s = %d", Constants.TEAMS_COL_ID, team.id));
	}

	public static boolean deleteTeam(int id) throws SQLException {
		return delete(Constants.TABLE_TEAMS, String.format("%s = %d", Constants.TEAMS_COL_ID, id));
	}

	public static boolean createTrackedTeam(long guildID, int teamID) throws SQLException {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.TRACKEDTEAMS_COL_GUILDID, guildID);
		values.put(Constants.TRACKEDTEAMS_COL_TEAMID, teamID);

		return create(Constants.TABLE_TRACKEDTEAMS, values);
	}

	public static List<BeamTeam> readTrackedTeamsForGuild(long guildID) throws SQLException {
		List<BeamTeam> teams = new ArrayList<BeamTeam>();

		List<List<String>> valueLists = read(Constants.TABLE_TEAMS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDTEAMS, Constants.TABLE_TEAMS,
						Constants.TEAMS_COL_ID, Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_TEAMID),
				String.format("%s.%s = %d", Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_GUILDID, guildID));

		for (List<String> values : valueLists) {
			BeamTeam team = new BeamTeam();

			team.id = Integer.parseInt(values.get(0));
			team.name = values.get(1);
			team.token = values.get(2);

			teams.add(team);
		}

		return teams;
	}

	public static List<BTBGuild> readGuildsForTrackedTeam(int teamID, boolean requireGoLive) throws SQLException {
		List<BTBGuild> guilds = new ArrayList<BTBGuild>();

		StringBuilder where = new StringBuilder(
				String.format("%s.%s = %d", Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_TEAMID, teamID));

		if (requireGoLive) {
			where.append(
					String.format(" AND %s.%s NOT NULL", Constants.TABLE_GUILDS, Constants.GUILDS_COL_GOLIVECHANNELID));
		}

		List<List<String>> valueLists = read(Constants.TABLE_GUILDS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDTEAMS, Constants.TABLE_GUILDS,
						Constants.GUILDS_COL_ID, Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_GUILDID),
				where.toString());

		for (List<String> values : valueLists) {
			BTBGuild guild = new BTBGuild(Long.parseLong(values.get(0)),
					GuildManager.getShardID(Long.parseLong(values.get(0))), values.get(1), values.get(2), values.get(3),
					"1".equals(values.get(4)));

			guilds.add(guild);
		}

		return guilds;
	}

	public static boolean deleteTrackedTeam(long guildID, int teamID) throws SQLException {
		return delete(Constants.TABLE_TRACKEDTEAMS, String.format("%s = %d AND %s = %d",
				Constants.TRACKEDTEAMS_COL_GUILDID, guildID, Constants.TRACKEDTEAMS_COL_TEAMID, teamID));
	}

	public static boolean createTrackedChannel(long guildID, int channelID) throws SQLException {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.TRACKEDCHANNELS_COL_GUILDID, guildID);
		values.put(Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID, channelID);

		return create(Constants.TABLE_TRACKEDCHANNELS, values);
	}

	public static List<BTBBeamChannel> readTrackedChannelsForGuild(long guildID) throws SQLException {
		List<BTBBeamChannel> channels = new ArrayList<BTBBeamChannel>();

		List<List<String>> valueLists = read(Constants.TABLE_CHANNELS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDCHANNELS, Constants.TABLE_CHANNELS,
						Constants.CHANNELS_COL_ID, Constants.TABLE_TRACKEDCHANNELS,
						Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID),
				String.format("%s.%s = %d", Constants.TABLE_TRACKEDCHANNELS, Constants.TRACKEDCHANNELS_COL_GUILDID,
						guildID));

		for (List<String> values : valueLists) {
			BTBBeamChannel channel = new BTBBeamChannel();

			channel.id = Integer.parseInt(values.get(0));
			channel.user = new BTBBeamUser();
			channel.user.username = values.get(1);
			channel.token = values.get(2);
			channel.userId = Integer.parseInt(values.get(3));

			channels.add(channel);
		}

		return channels;
	}

	public static List<BTBGuild> readGuildsForTrackedChannel(int channelID, boolean requireGoLive) throws SQLException {
		List<BTBGuild> guilds = new ArrayList<BTBGuild>();

		StringBuilder where = new StringBuilder(String.format("%s.%s = %d", Constants.TABLE_TRACKEDCHANNELS,
				Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID, channelID));

		if (requireGoLive) {
			where.append(
					String.format(" AND %s.%s NOT NULL", Constants.TABLE_GUILDS, Constants.GUILDS_COL_GOLIVECHANNELID));
		}

		List<List<String>> valueLists = read(Constants.TABLE_GUILDS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDCHANNELS, Constants.TABLE_GUILDS,
						Constants.GUILDS_COL_ID, Constants.TABLE_TRACKEDCHANNELS,
						Constants.TRACKEDCHANNELS_COL_GUILDID),
				where.toString());

		for (List<String> values : valueLists) {
			BTBGuild guild = new BTBGuild(Long.parseLong(values.get(0)),
					GuildManager.getShardID(Long.parseLong(values.get(0))), values.get(1), values.get(2), values.get(3),
					"1".equals(values.get(4)));

			guilds.add(guild);
		}

		return guilds;
	}

	public static boolean deleteTrackedChannel(long guildID, int channelID) throws SQLException {
		return delete(Constants.TABLE_TRACKEDCHANNELS,
				String.format("%s = %d AND %s = %d", Constants.TRACKEDCHANNELS_COL_GUILDID, guildID,
						Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID, channelID));
	}

	public static boolean createGoLiveMessage(GoLiveMessage message) throws SQLException {
		return create(Constants.TABLE_GOLIVEMESSAGES, message.getDbValues());
	}

	public static List<GoLiveMessage> readAllGoLiveMessagesForChannel(int channelID) throws SQLException {
		List<GoLiveMessage> messages = new ArrayList<>();

		List<List<String>> valueLists = read(Constants.TABLE_GOLIVEMESSAGES,
				new String[] { Constants.GOLIVEMESSAGES_COL_ID, Constants.GOLIVEMESSAGES_COL_GUILDID,
						Constants.GOLIVEMESSAGES_COL_GOLIVECHANNELID },
				null, String.format("%s = %d", Constants.GOLIVEMESSAGES_COL_BEAMCHANNELID, channelID));

		for (List<String> values : valueLists) {
			messages.add(new GoLiveMessage(values.get(0), values.get(1), Long.parseLong(values.get(2)),
					Integer.parseInt(values.get(3))));
		}

		return messages;
	}

	public static boolean deleteGoLiveMessagesForChannel(int channelID) throws SQLException {
		return delete(Constants.TABLE_GOLIVEMESSAGES,
				String.format("%s = %d", Constants.GOLIVEMESSAGES_COL_BEAMCHANNELID, channelID));
	}
}
