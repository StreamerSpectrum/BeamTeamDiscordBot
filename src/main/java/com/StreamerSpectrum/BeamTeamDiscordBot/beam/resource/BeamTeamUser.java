package com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public class BeamTeamUser implements Serializable {
	public Integer			level;
	public Social			social;
	public Integer			id;
	public String			username;
	public boolean			verified;
	public Integer			experience;
	public Integer			sparks;
	public String			avatarUrl;
	public String			bio;
	public Integer			primaryTeam;
	public Date				createdAt;
	public Date				updatedAt;
	public BTBBeamChannel			channel;

	@SerializedName("TeamMembership")
	public TeamMembership	teamMembership;

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BeamTeamUser && ((BeamTeamUser) obj).id == this.id)
				|| (obj instanceof BTBBeamUser && ((BTBBeamUser) obj).id == this.id);
	}
}
