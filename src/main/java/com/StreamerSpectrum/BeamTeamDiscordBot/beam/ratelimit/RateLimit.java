package com.StreamerSpectrum.BeamTeamDiscordBot.beam.ratelimit;

import java.util.Timer;
import java.util.TimerTask;

public class RateLimit {
	private final int REQUEST_COUNT;
	private final int TIME_INTERVAL;
	private volatile int curCount;
	private Timer timer;

	public RateLimit(int requestCount, int timeInterval) {
		REQUEST_COUNT = requestCount;
		TIME_INTERVAL = timeInterval;

		curCount = 0;

		timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				curCount = 0;
			}
		}, TIME_INTERVAL * 1000, TIME_INTERVAL * 1000);
	}

	public boolean isNotLimited() {
		return curCount++ < REQUEST_COUNT;
	}

}
