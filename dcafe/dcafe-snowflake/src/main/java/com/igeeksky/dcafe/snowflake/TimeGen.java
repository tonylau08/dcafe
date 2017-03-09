package com.igeeksky.dcafe.snowflake;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TimeGen {
	
	INSTANCE;
	
	private final Logger logger = LoggerFactory.getLogger(TimeGen.class);
	
	private ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
	
	private TimeUpdater updater = new TimeUpdater();
	
	private int period = 1000;
	
	private static long currMills = System.currentTimeMillis();
	
	private static long currSecond = currMills / 1000;
	
	private static final ReentrantLock TIME_LOCK = new ReentrantLock();
	
	private TimeGen(){
		es.scheduleAtFixedRate(updater, 0, period, TimeUnit.MILLISECONDS);
	}
	
	public long currMills(){
		return currMills; 
	}
	
	public long currSecond(){
		return currSecond;
	}
	
	long nextMills(long lastTime){
		try{
			TIME_LOCK.lock();
			if(currMills > lastTime){
				return currMills;
			}
			while(currMills <= lastTime){
				currMills = System.currentTimeMillis();
			}
			currSecond = currMills / 1000;
			return currMills;
		} finally{
			TIME_LOCK.unlock();
		}
	}
	
	private class TimeUpdater implements Runnable {

		@Override
		public void run() {
			try {
				nextMills(System.currentTimeMillis()-1);
			} catch (Exception e) {
				logger.error("TimeUpdater is error", e);
			}
		}
	}

}
