package com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class TeamMembership implements Serializable {
	public Integer	teamId;
	public Integer	userId;
	public boolean	accepted;
	public Date		createdAt;
	public Date		updatedAt;
}
