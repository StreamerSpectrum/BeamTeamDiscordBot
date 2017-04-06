package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public abstract class DbManager {

	private static Connection connection;

	public static Connection getConnection() {
		if (null == connection) {
			try {
				connection = DriverManager.getConnection("jdbc:sqlite:resources/bt.db");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return connection;
	}

	public static boolean closeDb() {
		try {
			getConnection().close();
			connection = null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null == connection;
	}

	public static boolean create(String tableName, Map<String, Object> values) throws SQLException {
		if (StringUtils.isBlank(tableName)) {
			return false;
		}

		Statement statement = null;

		StringBuilder columns = new StringBuilder();
		StringBuilder vals = new StringBuilder();
		for (String key : values.keySet()) {
			columns.append(key).append(", ");
			vals.append(String.format("'%s', ", values.get(key)));
		}

		try {
			statement = getConnection().createStatement();
			statement.setQueryTimeout(30);

			return statement.executeUpdate(String.format("INSERT INTO %s (%s) VALUES (%s);", tableName,
					columns.substring(0, columns.lastIndexOf(",")), vals.substring(0, vals.lastIndexOf(",")))) > 0;
		} finally {
			if (null != statement) {
				statement.close();
			}
		}
	}

	public static boolean create(String tableName, List<Map<String, Object>> valuesList) throws SQLException {
		if (StringUtils.isBlank(tableName)) {
			return false;
		}

		Statement statement = null;
		Set<String> keys = valuesList.get(0).keySet();

		StringBuilder columns = new StringBuilder();
		for (String key : keys) {
			keys.add(key);
			columns.append(key).append(", ");
		}

		StringBuilder vals = new StringBuilder();
		for (Map<String, Object> values : valuesList) {
			vals.append("(");

			for (String key : keys) {
				vals.append(String.format("'%s', ", values.get(key)));
			}

			vals.append("), ");
		}

		try {
			statement = getConnection().createStatement();
			statement.setQueryTimeout(30);

			return statement.executeUpdate(String.format("INSERT INTO %s (%s) VALUES %s;", tableName,
					columns.substring(0, columns.lastIndexOf(",")), vals.substring(0, vals.lastIndexOf(",")))) > 0;
		} finally {
			if (null != statement) {
				statement.close();
			}
		}
	}

	public static List<List<String>> read(String tableName, String[] columns, String innerJoin, String where)
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

	public static boolean update(String tableName, Map<String, Object> newVals, String where) throws SQLException {
		if (StringUtils.isBlank(tableName)) {
			return false;
		}

		Statement statement = null;

		StringBuilder sets = new StringBuilder();
		for (String key : newVals.keySet()) {
			sets.append(String.format("%s = '%s', ", key, newVals.get(key)));
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

	public static boolean delete(String tableName, String where) throws SQLException {
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
}
