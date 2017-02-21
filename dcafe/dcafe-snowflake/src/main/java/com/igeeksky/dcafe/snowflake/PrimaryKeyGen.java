package com.igeeksky.dcafe.snowflake;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>分布式自增长主键发生器</b><br>
 * 枚举单例，只允许公用一个实例。
 * @author Tony.Lau
 * @create 2016-12-23 09:50:41
 */
public enum PrimaryKeyGen {
	
	INSTANCE;
	
	private final Logger logger = LoggerFactory.getLogger(PrimaryKeyGen.class);

	private final Lock KEY_LOCK = new ReentrantLock();
	private int increment = 0;
	
	private AbstractRMConfig config;
	
	private long lastRmidTime;
	
	private long lastTime;
	
	private volatile boolean isFail = true;
	private int rmid = -1;
	
	/**
	 * <b>1bit (sign bit) + 41bit(time) + (roomId + machineId) + 12bit incr num</b><br>
	 */
	public long getIncrKey() {
		try {
			KEY_LOCK.lock();
			if (isFail) {
				return -1l;
			}
			if (increment >= 4096) {
				increment = 0;
				if((lastRmidTime = updateRmidTime()) < 0){
					throw new IllegalStateException("getIncrKey fail, KeyGenConfig is error");
				}else{
					return lastRmidTime ^ (increment++);
				}
			}else{
				if(lastRmidTime < 0){
					throw new IllegalStateException("getIncrKey fail, KeyGenConfig is error");
				}else{
					return lastRmidTime ^ (increment++);
				}
			}
		} finally {
			KEY_LOCK.unlock();
		}
	}
	
	/** update time with roomId and machineId */
	private long updateRmidTime() {
		try {
			KEY_LOCK.lock();
			long lastTimeTemp  = TimeGen.INSTANCE.nextTime(lastTime);
			long rmidTimeTemp = (lastTimeTemp << 23 >>> 1) ^ rmid;
			
			while (rmidTimeTemp <= lastRmidTime) {
				System.out.println("error");
				lastTimeTemp = TimeGen.INSTANCE.nextTime(lastTime);
				rmidTimeTemp = (lastTimeTemp << 23 >>> 1) ^ rmid;
			}
			lastTime = lastTimeTemp;
			return lastRmidTime = rmidTimeTemp;
		} finally {
			KEY_LOCK.unlock();
		}
	}

	/**
	 * <b>register config</b>
	 * @param config
	 * @return
	 */
	public RegisterState registerRoomMachine(AbstractRMConfig config) {
		isFail = true;
		if (config == null) {
			return RegisterState.ERROR;
		}
		if (config instanceof FailRMConfig) {
			return RegisterState.FAIL;
		}
		try {
			KEY_LOCK.lock();
			this.config = config;
			if (!updateRmid().equals(RegisterState.OK)) {
				logger.error("registerRoomMachine error");
				return RegisterState.ERROR;
			}
			isFail = false;
		} finally {
			KEY_LOCK.unlock();
		}
		logger.info("registerRoomMachine success");
		return RegisterState.OK;
	}

	/**
	 * <b>Register Status</b><br>
	 * <b>OK:</b>keyGen is running normal。<br>
	 * <b>FAIL:</b>register failConfig is success, keyGen is stop run。<br>
	 * <b>ERROR:</b>roomId or machineId is error，null object or other parm is error。<br>
	 */
	public enum RegisterState {
		OK, FAIL, ERROR;
	}
	
	/**
	 * <b> update roomId and machineId </b>
	 */
	private RegisterState updateRmid() {
		logger.debug("updateRmid()");
		int roomId = config.getRoomId();
		int roomBitNum = config.getRoomBitNum();

		int machineId = config.getMachineId();
		int machineBitNum = config.getMachineBitNum();

		if (roomId < 0 || machineId < 0) {
			isFail = true;
			logger.error("RoomId or MachineId < 0：roomId=" + roomId + "--machineId=" + machineId);
			return RegisterState.ERROR;
		}

		if (roomBitNum < 1 || machineBitNum < 1) {
			isFail = true;
			logger.error("roomBitNum or machineBitNum < 1：roomBitNum=" + roomBitNum + "--machineBitNum=" + machineBitNum);
			return RegisterState.ERROR;
		}

		if (roomBitNum + machineBitNum > 10) {
			isFail = true;
			logger.error("roomBitNum + machineBitNum > 10：roomBitNum=" + roomBitNum + "--machineBitNum=" + machineBitNum);
			return RegisterState.ERROR;
		}
		if (roomId >= (1 << roomBitNum)) {
			isFail = true;
			logger.error("RoomId bits > roomBitNum：" + roomId + ">=" + (1 << roomBitNum));
			return RegisterState.ERROR;
		}
		if (machineId >= (1 << machineBitNum)) {
			isFail = true;
			logger.error("machineId bits > machineBitNum" + machineId + ">=" + (1 << machineBitNum));
			return RegisterState.ERROR;
		}

		rmid = ((roomId << machineBitNum) ^ machineId) << 12;
		lastRmidTime = (System.currentTimeMillis() << 23 >>> 1) ^ rmid;
		return RegisterState.OK;
	}
	
}
