package com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public class TeamMembershipExpanded implements Serializable {
	public Social			social;
	public Integer			id;
	public Integer			ownerId;
	public String			token;
	public String			name;
	public String			description;
	public String			logoUrl;
	public String			backgroundUrl;
	public Integer			totalViewersCurrent;
	public Date				createdAt;
	public Date				updatedAt;
	public BTBBeamUser		owner;

	@SerializedName("TeamMembership")
	public TeamMembership	teamMembership;

}
