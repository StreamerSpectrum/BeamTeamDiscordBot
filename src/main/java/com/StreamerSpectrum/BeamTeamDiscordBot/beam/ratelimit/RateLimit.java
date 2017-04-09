package com.StreamerSpectrum.BeamTeamDiscordBot.beam.ratelimit;

import java.util.Timer;
import java.util.TimerTask;

public class RateLimit {
	private final int			REQUEST_COUNT;
	private final long			TIME_INTERVAL;
	private final String		NAME;

	private final SyncObject	syncObject		= new SyncObject();

	private Timer				timer			= new Timer();

	private volatile int		curCount;
	private volatile long		lastResetTime	= System.currentTimeMillis();

	public RateLimit(String name, int requestCount, long timeInterval) {
		NAME = name;
		REQUEST_COUNT = requestCount;
		TIME_INTERVAL = timeInterval * 1000;

		curCount = 0;

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (curCount > 0) {
					System.out.println(
							String.format("%s is resetting its rate limit (%d/%d).", NAME, curCount, REQUEST_COUNT));
					curCount = 0;
					lastResetTime = System.currentTimeMillis();
					synchronized (syncObject) {
						syncObject.notify();
					}
				}
			}
		}, TIME_INTERVAL, TIME_INTERVAL);
	}

	public boolean isNotLimited() throws InterruptedException {
		if (curCount >= REQUEST_COUNT) {
			synchronized (syncObject) {
				System.out.println(String.format("%s has reached its rate limit (%d/%d). Reset in %ds", NAME, curCount,
						REQUEST_COUNT, ((lastResetTime + TIME_INTERVAL) - System.currentTimeMillis()) / 1000));
				syncObject.wait();
			}
		}

		return curCount++ < REQUEST_COUNT;
	}

}

class SyncObject {}
