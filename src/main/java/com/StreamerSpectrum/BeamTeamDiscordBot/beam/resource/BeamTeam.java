package com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
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
    
    @Override
    public int hashCode() {
    	return id.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	return obj instanceof BeamTeam && ((BeamTeam) obj).id == this.id;
    }
}
