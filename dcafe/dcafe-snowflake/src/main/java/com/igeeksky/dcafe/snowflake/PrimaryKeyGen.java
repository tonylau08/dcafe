package com.igeeksky.dcafe.snowflake;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <b>分布式自增长主键发生器</b><br>
 * 枚举单例，只允许公用一个实例。
 * @author Tony.Lau
 * @create 2016-12-23 09:50:41
 */
public enum PrimaryKeyGen {
	
	INSTANCE;
	
	private final Lock KEY_LOCK = new ReentrantLock();
	
	private int increment = 0;
	
	private long lastTime = TimeGen.INSTANCE.currMills();
	
	/**
	 * <b>1bit (sign bit) + 41bit(time) + (roomId + machineId) + 12bit incr num</b><br>
	 */
	public long getIncrKey() {
		try {
			KEY_LOCK.lock();
			if (increment >= 4096) {
				increment = 0;
				lastTime = TimeGen.INSTANCE.nextMills(lastTime);
			}else{
				lastTime = TimeGen.INSTANCE.currMills();
			}
			long lastTimePrefix = lastTime << 23 >>> 1;
			return lastTimePrefix ^ RoomMachineRegister.INSTANCE.getRmid() ^ (increment++);
		} finally {
			KEY_LOCK.unlock();
		}
	}
	
}
