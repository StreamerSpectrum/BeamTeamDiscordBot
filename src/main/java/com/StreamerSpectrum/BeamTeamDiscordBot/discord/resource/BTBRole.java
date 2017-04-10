package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import java.util.HashMap;
import java.util.Map;

import com.StreamerSpectrum.BeamTeamDiscordBot.Constants;

public class BTBRole {
	private final long	guildID;
	private int			teamID;
	private String		roleID;

	public BTBRole(long guildID, int teamID, String roleID) {
		this.guildID = guildID;
		this.teamID = teamID;
		this.roleID = roleID;
	}

	public long getGuildID() {
		return guildID;
	}

	public int getTeamID() {
		return teamID;
	}

	public void setTeamID(int teamID) {
		this.teamID = teamID;
	}

	public String getRoleID() {
		return roleID;
	}

	public void setRoleID(String roleID) {
		this.roleID = roleID;
	}

	public Map<String, Object> getDbValues() {
		Map<String, Object> values = new HashMap<>();

		values.put(Constants.TEAMROLES_COL_GUILDID, getGuildID());
		values.put(Constants.TEAMROLES_COL_ROLEID, getRoleID());
		values.put(Constants.TEAMROLES_COL_TEAMID, getTeamID());

		return values;
	}
}
