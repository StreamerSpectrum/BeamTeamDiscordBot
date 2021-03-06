package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sqlite.SQLiteConfig;

import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeam;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBGuild;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBRole;
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

				SQLiteConfig config = new SQLiteConfig();
				config.enforceForeignKeys(true);
				connection = DriverManager.getConnection("jdbc:sqlite:resources/bt.db", config.toProperties());
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

	private static boolean create(String tableName, Map<String, Object> values) {
		if (StringUtils.isBlank(tableName)) {
			return false;
		}

		StringBuilder columns = new StringBuilder();
		StringBuilder vals = new StringBuilder();
		for (String key : values.keySet()) {
			columns.append(key).append(", ");
			vals.append("?, ");
		}

		PreparedStatement statement = null;

		try {
			try {
				statement = getConnection().prepareStatement(String.format("INSERT INTO %s (%s) VALUES (%s);",
						tableName, columns.substring(0, columns.lastIndexOf(",")),
						vals.substring(0, vals.lastIndexOf(","))));
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
		} catch (SQLException e) {
			// TODO: Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	private static List<List<String>> read(String tableName, String[] columns, String innerJoin,
			Map<String, Object> where) {
		if (StringUtils.isBlank(tableName)) {
			return new ArrayList<>();
		}

		StringBuilder cols = new StringBuilder();
		if (null != columns && columns.length > 0) {
			for (String columnName : columns) {
				if (StringUtils.isNotBlank(columnName)) {
					cols.append(columnName).append(", ");
				}
			}
		}

		StringBuilder sql = new StringBuilder();
		sql.append(String.format("SELECT %s FROM %s",
				cols.length() > 0 ? cols.substring(0, cols.lastIndexOf(",")) : "*", tableName));

		if (StringUtils.isNotBlank(innerJoin)) {
			sql.append(String.format(" INNER JOIN %s", innerJoin));
		}

		if (null != where && !where.isEmpty()) {
			StringBuilder whereBuilder = new StringBuilder(" WHERE ");

			for (String key : where.keySet()) {
				if (StringUtils.contains(where.get(key).toString(), "NULL")) {
					whereBuilder.append(String.format("%s %s AND ", key, where.get(key)));
				} else {
					whereBuilder.append(String.format("%s = ? AND ", key));
				}
			}

			sql.append(String.format("%s", whereBuilder.substring(0, whereBuilder.lastIndexOf(" AND "))));
		}

		sql.append(";");

		PreparedStatement statement = null;

		try {
			try {
				statement = getConnection().prepareStatement(sql.toString());
				statement.setQueryTimeout(30);

				if (null != where && !where.isEmpty()) {
					int i = 1;
					for (String key : where.keySet()) {
						if (!StringUtils.contains(where.get(key).toString(), "NULL")) {
							statement.setObject(i++, where.get(key));
						}
					}
				}

				ResultSet rs = statement.executeQuery();
				List<List<String>> values = new ArrayList<>();

				while (rs.next()) {
					List<String> vals = new ArrayList<>();

					if (null != columns && columns.length > 0) {
						for (String columnName : columns) {
							vals.add(rs.getString(columnName));
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
		} catch (SQLException e) {
			// TODO: Auto-generated catch block
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	private static boolean update(String tableName, Map<String, Object> newVals, Map<String, Object> where) {
		if (StringUtils.isBlank(tableName) || null == newVals || newVals.isEmpty()) {
			return false;
		}

		StringBuilder sets = new StringBuilder();
		for (String key : newVals.keySet()) {
			sets.append(String.format("%s = ?, ", key));
		}

		StringBuilder sql = new StringBuilder();
		sql.append(String.format("UPDATE %s SET %s", tableName, sets.substring(0, sets.lastIndexOf(","))));

		if (null != where && !where.isEmpty()) {
			StringBuilder whereBuilder = new StringBuilder(" WHERE ");

			for (String key : where.keySet()) {
				if (StringUtils.contains(where.get(key).toString(), "NULL")) {
					whereBuilder.append(String.format("%s %s AND ", key, where.get(key)));
				} else {
					whereBuilder.append(String.format("%s = ? AND ", key));
				}
			}

			sql.append(String.format("%s", whereBuilder.substring(0, whereBuilder.lastIndexOf(" AND "))));
		}

		sql.append(";");

		PreparedStatement statement = null;

		try {
			try {
				statement = getConnection().prepareStatement(sql.toString());
				statement.setQueryTimeout(30);

				int i = 1;
				for (String key : newVals.keySet()) {
					statement.setObject(i++, newVals.get(key));
				}

				if (null != where && !where.isEmpty()) {
					for (String key : where.keySet()) {
						if (!StringUtils.contains(where.get(key).toString(), "NULL")) {
							statement.setObject(i++, where.get(key));
						}
					}
				}

				return statement.executeUpdate() > 0;
			} finally {
				if (null != statement) {
					statement.close();
				}
			}
		} catch (SQLException e) {
			// TODO: Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	private static boolean delete(String tableName, Map<String, Object> where) {
		if (StringUtils.isBlank(tableName)) {
			return false;
		}

		StringBuilder sql = new StringBuilder(String.format("DELETE FROM %s", tableName));

		if (null != where && !where.isEmpty()) {
			StringBuilder whereBuilder = new StringBuilder(" WHERE ");

			for (String key : where.keySet()) {
				if (StringUtils.contains(where.get(key).toString(), "NULL")) {
					whereBuilder.append(String.format("%s %s AND ", key, where.get(key)));
				} else {
					whereBuilder.append(String.format("%s = ? AND ", key));
				}
			}

			sql.append(String.format("%s", whereBuilder.substring(0, whereBuilder.lastIndexOf(" AND "))));
		}

		sql.append(";");

		PreparedStatement statement = null;

		try {
			try {
				statement = getConnection().prepareStatement(sql.toString());
				statement.setQueryTimeout(30);

				if (null != where && !where.isEmpty()) {
					int i = 1;
					for (String key : where.keySet()) {
						if (!StringUtils.contains(where.get(key).toString(), "NULL")) {
							statement.setObject(i++, where.get(key));
						}
					}
				}

				return statement.executeUpdate() > 0;

			} finally {
				if (null != statement) {
					statement.close();
				}
			}
		} catch (SQLException e) {
			// TODO: Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public static int readVersion() {
		return Integer.parseInt(read(Constants.TABLE_VERSION, new String[] { "ID" }, null, null).get(0).get(0));
	}

	public static boolean createGuild(BTBGuild guild) {
		return create(Constants.TABLE_GUILDS, guild.getDbValues());
	}

	public static BTBGuild readGuild(long id) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.GUILDS_COL_ID, id);

		List<List<String>> values = read(Constants.TABLE_GUILDS, null, null, where);

		BTBGuild guild = null;

		if (!values.isEmpty()) {
			guild = new BTBGuild(id, GuildManager.getShardID(id), values.get(0).get(1), values.get(0).get(2),
					values.get(0).get(3), values.get(0).get(4), "1".equals((values.get(0).get(5))));
		}

		return guild;
	}

	public static List<BTBGuild> readAllGuilds() {
		List<BTBGuild> guilds = new ArrayList<BTBGuild>();
		List<List<String>> valuesList = read(Constants.TABLE_GUILDS, null, null, null);

		for (List<String> values : valuesList) {
			guilds.add(
					new BTBGuild(Long.parseLong(values.get(0)), GuildManager.getShardID(Long.parseLong(values.get(0))),
							values.get(1), values.get(2), values.get(3), values.get(4), "1".equals((values.get(5)))));
		}

		return guilds;
	}

	public static List<BTBGuild> readAllGuilds(Map<String, Object> where) {
		List<BTBGuild> guilds = new ArrayList<BTBGuild>();
		List<List<String>> valuesList = read(Constants.TABLE_GUILDS, null, null, where);

		for (List<String> values : valuesList) {
			guilds.add(
					new BTBGuild(Long.parseLong(values.get(0)), GuildManager.getShardID(Long.parseLong(values.get(0))),
							values.get(1), values.get(2), values.get(3), values.get(4), "1".equals((values.get(5)))));
		}

		return guilds;
	}

	public static boolean updateGuild(BTBGuild guild) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.GUILDS_COL_ID, guild.getID());

		return update(Constants.TABLE_GUILDS, guild.getDbValues(), where);
	}

	public static boolean deleteGuild(long id) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.GUILDS_COL_ID, id);

		return delete(Constants.TABLE_GUILDS, where);
	}

	public static boolean createChannel(BTBBeamChannel channel) {
		return create(Constants.TABLE_CHANNELS, channel.getDbValues());
	}

	public static BTBBeamChannel readChannel(int id) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.CHANNELS_COL_ID, id);

		List<List<String>> values = read(Constants.TABLE_CHANNELS, null, null, where);

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

	public static List<BTBBeamChannel> readAllChannels() {
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

	public static boolean updateChannel(BTBBeamChannel channel) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.CHANNELS_COL_ID, channel.id);

		return update(Constants.TABLE_CHANNELS, channel.getDbValues(), where);
	}

	public static boolean deleteChannel(int id) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.CHANNELS_COL_ID, id);

		return delete(Constants.TABLE_CHANNELS, where);
	}

	public static boolean createTeam(BeamTeam team) {
		return create(Constants.TABLE_TEAMS, team.getDbValues());
	}

	public static BeamTeam readTeam(int id) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.TEAMS_COL_ID, id);

		List<List<String>> values = read(Constants.TABLE_TEAMS, null, null, where);

		BeamTeam team = null;

		if (!values.isEmpty()) {
			team = new BeamTeam();

			team.id = Integer.parseInt(values.get(0).get(0));
			team.name = values.get(0).get(1);
			team.token = values.get(0).get(2);
		}

		return team;
	}

	public static List<BeamTeam> readAllTeams() {
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

	public static boolean updateTeam(BeamTeam team) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.TEAMS_COL_ID, team.id);

		return update(Constants.TABLE_TEAMS, team.getDbValues(), where);
	}

	public static boolean deleteTeam(int id) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.TEAMS_COL_ID, id);

		return delete(Constants.TABLE_TEAMS, where);
	}

	public static boolean createTrackedTeam(long guildID, int teamID) {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.TRACKEDTEAMS_COL_GUILDID, guildID);
		values.put(Constants.TRACKEDTEAMS_COL_TEAMID, teamID);

		return create(Constants.TABLE_TRACKEDTEAMS, values);
	}

	public static List<BeamTeam> readTrackedTeamsForGuild(long guildID) {
		List<BeamTeam> teams = new ArrayList<BeamTeam>();

		Map<String, Object> where = new HashMap<>();
		where.put(String.format("%s.%s", Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_GUILDID), guildID);

		List<List<String>> valueLists = read(Constants.TABLE_TEAMS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDTEAMS, Constants.TABLE_TEAMS,
						Constants.TEAMS_COL_ID, Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_TEAMID),
				where);

		for (List<String> values : valueLists) {
			BeamTeam team = new BeamTeam();

			team.id = Integer.parseInt(values.get(0));
			team.name = values.get(1);
			team.token = values.get(2);

			teams.add(team);
		}

		return teams;
	}

	public static List<BTBGuild> readGuildsForTrackedTeam(int teamID, boolean requireGoLive, boolean requireLogChannel,
			boolean requireNewMemberChannel) {
		List<BTBGuild> guilds = new ArrayList<BTBGuild>();
		Map<String, Object> where = new HashMap<>();

		where.put(String.format("%s.%s", Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_TEAMID), teamID);

		if (requireGoLive) {
			where.put(String.format("%s.%s", Constants.TABLE_GUILDS, Constants.GUILDS_COL_GOLIVECHANNELID),
					"IS NOT NULL");
		}

		if (requireLogChannel) {
			where.put(String.format("%s.%s", Constants.TABLE_GUILDS, Constants.GUILDS_COL_LOGCHANNELID), "IS NOT NULL");
		}

		if (requireNewMemberChannel) {
			where.put(String.format("%s.%s", Constants.TABLE_GUILDS, Constants.GUILDS_COL_NEWMEMBERCHANNELID),
					"IS NOT NULL");
		}

		List<List<String>> valueLists = read(Constants.TABLE_GUILDS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDTEAMS, Constants.TABLE_GUILDS,
						Constants.GUILDS_COL_ID, Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_GUILDID),
				where);

		for (List<String> values : valueLists) {
			BTBGuild guild = new BTBGuild(Long.parseLong(values.get(0)),
					GuildManager.getShardID(Long.parseLong(values.get(0))), values.get(1), values.get(2), values.get(3),
					values.get(4), "1".equals(values.get(5)));

			guilds.add(guild);
		}

		return guilds;
	}

	public static List<BeamTeam> readAllTrackedTeams() {
		List<BeamTeam> teams = new ArrayList<>();

		List<List<String>> valueLists = read(Constants.TABLE_TEAMS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDTEAMS, Constants.TABLE_TEAMS,
						Constants.TEAMS_COL_ID, Constants.TABLE_TRACKEDTEAMS, Constants.TRACKEDTEAMS_COL_TEAMID),
				null);

		for (List<String> values : valueLists) {
			BeamTeam team = new BeamTeam();

			team.id = Integer.parseInt(values.get(0));
			team.name = values.get(1);
			team.token = values.get(2);

			teams.add(team);
		}

		return teams;
	}

	public static boolean deleteTrackedTeam(long guildID, int teamID) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.TRACKEDTEAMS_COL_GUILDID, guildID);
		where.put(Constants.TRACKEDTEAMS_COL_TEAMID, teamID);

		return delete(Constants.TABLE_TRACKEDTEAMS, where);
	}

	public static boolean createTrackedChannel(long guildID, int channelID) {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.TRACKEDCHANNELS_COL_GUILDID, guildID);
		values.put(Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID, channelID);

		return create(Constants.TABLE_TRACKEDCHANNELS, values);
	}

	public static List<BTBBeamChannel> readTrackedChannelsForGuild(long guildID) {
		List<BTBBeamChannel> channels = new ArrayList<BTBBeamChannel>();
		Map<String, Object> where = new HashMap<>();
		where.put(String.format("%s.%s", Constants.TABLE_TRACKEDCHANNELS, Constants.TRACKEDCHANNELS_COL_GUILDID),
				guildID);

		List<List<String>> valueLists = read(Constants.TABLE_CHANNELS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDCHANNELS, Constants.TABLE_CHANNELS,
						Constants.CHANNELS_COL_ID, Constants.TABLE_TRACKEDCHANNELS,
						Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID),
				where);

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

	public static List<BTBGuild> readGuildsForTrackedChannel(int channelID, boolean requireGoLive,
			boolean requireLogChannel, boolean requireNewMemberChannel) {
		List<BTBGuild> guilds = new ArrayList<BTBGuild>();

		Map<String, Object> where = new HashMap<>();
		where.put(String.format("%s.%s", Constants.TABLE_TRACKEDCHANNELS, Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID),
				channelID);

		if (requireGoLive) {
			where.put(String.format("%s.%s", Constants.TABLE_GUILDS, Constants.GUILDS_COL_GOLIVECHANNELID),
					"IS NOT NULL");
		}

		if (requireLogChannel) {
			where.put(String.format("%s.%s", Constants.TABLE_GUILDS, Constants.GUILDS_COL_LOGCHANNELID), "IS NOT NULL");
		}

		if (requireNewMemberChannel) {
			where.put(String.format("%s.%s", Constants.TABLE_GUILDS, Constants.GUILDS_COL_NEWMEMBERCHANNELID),
					"IS NOT NULL");
		}

		List<List<String>> valueLists = read(Constants.TABLE_GUILDS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDCHANNELS, Constants.TABLE_GUILDS,
						Constants.GUILDS_COL_ID, Constants.TABLE_TRACKEDCHANNELS,
						Constants.TRACKEDCHANNELS_COL_GUILDID),
				where);

		for (List<String> values : valueLists) {
			BTBGuild guild = new BTBGuild(Long.parseLong(values.get(0)),
					GuildManager.getShardID(Long.parseLong(values.get(0))), values.get(1), values.get(2), values.get(3),
					values.get(4), "1".equals(values.get(5)));

			guilds.add(guild);
		}

		return guilds;
	}

	public static List<BTBBeamChannel> readAllTrackedChannels() {
		List<BTBBeamChannel> channels = new ArrayList<>();

		List<List<String>> valueLists = read(Constants.TABLE_CHANNELS, null,
				String.format("%s ON %s.%s = %s.%s", Constants.TABLE_TRACKEDCHANNELS, Constants.TABLE_CHANNELS,
						Constants.CHANNELS_COL_ID, Constants.TABLE_TRACKEDCHANNELS,
						Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID),
				null);

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

	public static boolean deleteTrackedChannel(long guildID, int channelID) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.TRACKEDCHANNELS_COL_GUILDID, guildID);
		where.put(Constants.TRACKEDCHANNELS_COL_BEAMCHANNELID, channelID);

		return delete(Constants.TABLE_TRACKEDCHANNELS, where);
	}

	public static boolean createGoLiveMessage(GoLiveMessage message) {
		return create(Constants.TABLE_GOLIVEMESSAGES, message.getDbValues());
	}

	public static List<GoLiveMessage> readAllGoLiveMessages() {
		List<GoLiveMessage> messages = new ArrayList<>();

		List<List<String>> valueLists = read(Constants.TABLE_GOLIVEMESSAGES, null, null, null);

		for (List<String> values : valueLists) {
			messages.add(new GoLiveMessage(values.get(0), values.get(1), Long.parseLong(values.get(2)),
					Integer.parseInt(values.get(3))));
		}

		return messages;
	}

	public static List<GoLiveMessage> readAllGoLiveMessagesForChannel(int channelID) {
		List<GoLiveMessage> messages = new ArrayList<>();

		Map<String, Object> where = new HashMap<>();
		where.put(Constants.GOLIVEMESSAGES_COL_BEAMCHANNELID, channelID);

		List<List<String>> valueLists = read(Constants.TABLE_GOLIVEMESSAGES, null, null, where);

		for (List<String> values : valueLists) {
			messages.add(new GoLiveMessage(values.get(0), values.get(1), Long.parseLong(values.get(2)),
					Integer.parseInt(values.get(3))));
		}

		return messages;
	}

	public static boolean deleteAllGoLiveMessages() {
		return delete(Constants.TABLE_GOLIVEMESSAGES, null);
	}

	public static boolean deleteGoLiveMessagesForChannel(int channelID) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.GOLIVEMESSAGES_COL_BEAMCHANNELID, channelID);

		return delete(Constants.TABLE_GOLIVEMESSAGES, where);
	}

	public static boolean createTeamRole(BTBRole role) {
		return create(Constants.TABLE_TEAMROLES, role.getDbValues());
	}

	public static List<BTBRole> readTeamRolesForGuild(long guildID) {
		List<BTBRole> roles = new ArrayList<>();
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.TEAMROLES_COL_GUILDID, guildID);

		List<List<String>> valuesList = read(Constants.TABLE_TEAMROLES, null, null, where);

		for (List<String> values : valuesList) {
			roles.add(new BTBRole(Long.parseLong(values.get(0)), Integer.parseInt(values.get(1)), values.get(2)));
		}

		return roles;
	}

	public static List<BTBRole> readTeamRolesForTeam(int teamID) {
		List<BTBRole> roles = new ArrayList<>();
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.TEAMROLES_COL_TEAMID, teamID);

		List<List<String>> valuesList = read(Constants.TABLE_TEAMROLES, null, null, where);

		for (List<String> values : valuesList) {
			roles.add(new BTBRole(Long.parseLong(values.get(0)), Integer.parseInt(values.get(1)), values.get(3)));
		}

		return roles;
	}

	public static boolean deleteTeamRole(BTBRole role) {
		return delete(Constants.TABLE_TEAMROLES, role.getDbValues());
	}

	public static boolean deleteTeamRole(long guildID, int teamID) {
		Map<String, Object> where = new HashMap<>();
		where.put(Constants.TEAMROLES_COL_GUILDID, guildID);
		where.put(Constants.TEAMROLES_COL_TEAMID, teamID);

		return delete(Constants.TABLE_TEAMROLES, where);
	}
}
