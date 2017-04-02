package com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.annotations.SerializedName;
import pro.beam.api.resource.channel.BeamResource;

@SuppressWarnings("serial")
public class BTBBeamUser implements Serializable {
    public Date createdAt;
    public String email;
    public int id;
    public BTBBeamChannel channel;
    public int points;
    public int experience;
    public int sparks;
    public Date resetTime;
    public Social social;
    public Date updatedAt;
    public String username;
    public boolean verified;
    public ArrayList<BeamResource> avatars;
    public String bio;

    public enum Role {
        @SerializedName("Banned") BANNED,
        @SerializedName("Muted") MUTED,
        @SerializedName("User") USER,
        @SerializedName("Pro") PRO,
        @SerializedName("Subscriber") SUBSCRIBER,
        @SerializedName("Mod") MOD,
        @SerializedName("Global Mod") GLOBAL_MOD,
        @SerializedName("Founder") FOUNDER,
        @SerializedName("Staff") STAFF,
        @SerializedName("Owner") OWNER
    }

    public class Social implements Serializable {
        public String facebook;
        public String twitter;
        public String youtube;
        public String discord;
    }
}
