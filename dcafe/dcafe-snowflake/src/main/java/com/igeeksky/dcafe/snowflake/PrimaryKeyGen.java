package com.igeeksky.dcafe.snowflake;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <b>分布式自增长主键发生器</b><br>
 * 允许根据不同的表new多个实例，或者多表公用一个实例。
 * @author Tony.Lau
 * @create 2016-12-23 09:50:41
 */
public enum PrimaryKeyGen {
	
	INSTANCE;

	private final Lock INCR_LOCK = new ReentrantLock();
	private int increment = 0;
	
	/**
	 * <b>1bit符号位 + 41bit时间 + 机房ID + 机器ID + 12bit自增长ID</b><br>
	 * @return 如果返回值小于等于0，则表示系统环境错误；大于0为正常值。
	 */
	public long getIncrKey() {
		try {
			INCR_LOCK.lock();
			long time = 0l;
			if (increment >= 4096) {
				increment = 0;
				if((time = TimeGenerator.INSTANCE.updateTime()) < 0){
					return -1l;
				}else{
					return time ^ (increment++);
				}
			}else{
				if((time = TimeGenerator.INSTANCE.getTime()) < 0){
					return -1l;
				}else{
					return time ^ (increment++);
				}
			}
		} finally {
			INCR_LOCK.unlock();
		}
	}
	
}
