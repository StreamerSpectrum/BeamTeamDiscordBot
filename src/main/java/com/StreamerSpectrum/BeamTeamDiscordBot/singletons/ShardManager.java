package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.util.HashMap;

import net.dv8tion.jda.core.JDA;

//TODO: Finish This and replace Crocs JDAManager.
public class ShardManager {
	
	private static ShardManager inst = null;
	private int totalShards;
	private int currentLiveShards = 0;
	private HashMap<Integer,JDA> shards;
	
	public static void createInstance(){
		if(inst != null)
			return;
	
		inst = new ShardManager();
	}
	
	private ShardManager(){
		
		
		
	}
	
	public static ShardManager getInstance(){
		if(inst == null)
			createInstance();
		return inst;
	}
	
	public void createShards(int endShard){
		
	}
	
	public void createShards(int startShard, int endShard){
		
	}
	
	
}
