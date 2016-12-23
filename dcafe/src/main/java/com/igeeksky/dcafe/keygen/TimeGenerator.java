package com.igeeksky.dcafe.keygen;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分布式时间发生器
 * 
 * @author Tony.Lau
 * @create 2016-12-23 09:51:37
 */
public enum TimeGenerator {

	INSTANCE;

	private Logger logger = LoggerFactory.getLogger(TimeGenerator.class);

	private volatile AbstractRMConfig config;
	private volatile long lastTimeMills;
	private volatile boolean isFail = true;
	private volatile int rmid = -1;

	private final Lock rmidLock = new ReentrantLock();

	private ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
	private volatile boolean isRun = false;

	long getTime() {
		try {
			rmidLock.lock();
			if (isFail) {
				return -1l;
			}
			return lastTimeMills;
		} finally {
			rmidLock.unlock();
		}
	}

	long updateTime() {
		try {
			rmidLock.lock();
			if (isFail) {
				return -1l;
			}
			long temp = (System.currentTimeMillis() << 23 >>> 1) ^ rmid;
			while (temp == lastTimeMills) {
				temp = (System.currentTimeMillis() << 23 >>> 1) ^ rmid;
			}
			return lastTimeMills = temp;
		} finally {
			rmidLock.unlock();
		}
	}

	public RegisterState registerRoomMachine(AbstractRMConfig config) {
		try {
			rmidLock.lock();
			if (config == null) {
				isFail = true;
				return RegisterState.ERROR;
			}
			if (config instanceof FailRMConfig) {
				isFail = true;
				return RegisterState.FAIL;
			}

			this.config = config;
			if (!updateRmid().equals(RegisterState.OK)) {
				logger.error("registerRoomMachine error");
				return RegisterState.ERROR;
			}
			if (!isRun) {
				int timePeriod = config.getTimeUpdatePeriod();
				if(timePeriod < 1){
					logger.error("getTimeUpdatePeriod error:" + timePeriod + "<1");
					return RegisterState.ERROR;
				}
				es.scheduleAtFixedRate(new TimeUpdater(), 0, timePeriod, TimeUnit.MILLISECONDS);
				isRun = true;
			}
		} finally {
			rmidLock.unlock();
		}
		logger.info("registerRoomMachine success");
		return RegisterState.OK;
	}

	private RegisterState updateRmid() {
		logger.debug("updateRmid()");
		int roomId = config.getRoomId();
		int roomBitNum = config.getRoomBitNum();

		int machineId = config.getMachineId();
		int machineBitNum = config.getMachineBitNum();

		if (roomId < 0 || machineId < 0) {
			isFail = true;
			logger.error("房间ID 或 机器ID不能小于0：roomId=" + roomId + "--machineId=" + machineId);
			return RegisterState.ERROR;
		}

		if (roomBitNum < 1 || machineBitNum < 1) {
			isFail = true;
			logger.error("房间ID位数 或 机器ID位数不能小于1：roomBitNum=" + roomBitNum + "--machineBitNum=" + machineBitNum);
			return RegisterState.ERROR;
		}

		if (roomBitNum + machineBitNum > 10) {
			isFail = true;
			logger.error("房间ID+机器ID组合后位数不能超过10位：roomBitNum=" + roomBitNum + "--machineBitNum=" + machineBitNum);
			return RegisterState.ERROR;
		}
		if (roomId >= (1 << roomBitNum)) {
			isFail = true;
			logger.error("机房ID超过设定数值：" + roomId + ">=" + (1 << roomBitNum));
			return RegisterState.ERROR;
		}
		if (machineId >= (1 << machineBitNum)) {
			isFail = true;
			logger.error("机器ID超过设定数值" + machineId + ">=" + (1 << machineBitNum));
			return RegisterState.ERROR;
		}

		rmid = ((roomId << machineBitNum) ^ machineId) << 12;
		lastTimeMills = (System.currentTimeMillis() << 23 >>> 1) ^ rmid;
		isFail = false;
		return RegisterState.OK;
	}

	/**
	 * <b>注册状态</b><br>
	 * OK：注册机房ID和机器ID成功，可以开始获取主键。<br>
	 * FAIL：注册Fail对象成功，系统停止产生正确主键，全部返回-1。<br>
	 * ERROR：注册机房ID和机器ID失败，空对象或者参数错误，系统无法产生正确主键，全部返回-1。<br>
	 * 
	 * @create 2016-12-22 21:06:35
	 */
	public enum RegisterState {
		OK, FAIL, ERROR;
	}

	/**
	 * <b>时间定时更新器</b><br>
	 * 
	 * @create 2016-12-22 22:09:45
	 */
	private class TimeUpdater implements Runnable {

		@Override
		public void run() {
			try {
				updateTime();
			} catch (Exception e) {
				logger.error("定时更新时间发生错误", e);
			}
		}
	}

}
