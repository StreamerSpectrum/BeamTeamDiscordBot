package com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource;

import java.io.Serializable;
import java.util.Date;

public class BeamTeam implements Serializable {
	public Integer id;
	public Integer ownerId;
	public String token;
	public String name;
	public String description;
	public String logoUrl;
	public String backgroundUrl;
	public Integer totalViewersCurrent;
    public Date createdAt;
    public Date updatedAt;
	public Social social;

    public class Social implements Serializable {
        public String facebook;
        public String twitter;
        public String youtube;
        public String discord;
        public String player;
    }
}
