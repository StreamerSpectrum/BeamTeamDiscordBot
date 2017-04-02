package com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource;

import com.StreamerSpectrum.BeamTeamDiscordBot.beam.constellation.Constellation;;

public class Guild {

	private final long		id;

	private Tracker			Tracker;
	private Options			options;
	private Constellation	constellation;

	public Guild(long id) {
		this.id = id;
	}

	public long getID() {
		return id;
	}

	public Tracker getTracker() {
		if (null == Tracker) {
			Tracker = new Tracker(this);

			// TODO: load stored teams from DB
		}

		return Tracker;
	}

	public Options getOptions() {
		if (null == options) {
			options = new Options(this);

			// TODO: load stored options from DB
		}

		return options;
	}

	public Constellation getConstellation() {
		if (null == constellation) {
			constellation = new Constellation(this);

			// TODO: subscribe to team members and stuff if certain options are
			// set
		}

		return constellation;
	}

}
