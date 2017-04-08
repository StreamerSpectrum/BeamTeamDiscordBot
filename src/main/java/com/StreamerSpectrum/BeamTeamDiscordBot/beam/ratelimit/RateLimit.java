package com.StreamerSpectrum.BeamTeamDiscordBot.beam.ratelimit;

import java.util.Timer;
import java.util.TimerTask;

public class RateLimit {
	private final int			REQUEST_COUNT;
	private final int			TIME_INTERVAL;
	private final String		NAME;

	private final SyncObject	syncObject	= new SyncObject();

	private Timer				timer		= new Timer();

	private volatile int		curCount;

	public RateLimit(String name, int requestCount, int timeInterval) {
		NAME = name;
		REQUEST_COUNT = requestCount;
		TIME_INTERVAL = timeInterval;

		curCount = 0;

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				curCount = 0;
				synchronized (syncObject) {
					syncObject.notify();
					System.out.println(String.format("%s has reset its rate limit (%d/%d).", NAME, curCount, REQUEST_COUNT));
				}
			}
		}, TIME_INTERVAL * 1000, TIME_INTERVAL * 1000);
	}

	public boolean isNotLimited() throws InterruptedException {
		if (curCount >= REQUEST_COUNT) {
			synchronized (syncObject) {
				// TODO: notify log channel that we're waiting for the rate
				// limit to chill
				System.out.println(String.format("%s has reached its rate limit (%d/%d).", NAME, curCount, REQUEST_COUNT));
				syncObject.wait();
			}
		}

		return curCount++ < REQUEST_COUNT;
	}

}

class SyncObject {}
