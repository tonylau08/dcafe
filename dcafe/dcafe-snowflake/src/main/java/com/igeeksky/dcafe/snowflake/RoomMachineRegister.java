/*
 * Copyright 2017 Tony.lau All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igeeksky.dcafe.snowflake;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-28 23:51:12
 */

public enum RoomMachineRegister {
	
	INSTANCE;
	
	private int rmid = -1;
	
	private volatile boolean isFail = true;
	
	private final Lock RMID_LOCK = new ReentrantLock(); 
	
	private AbstractRMConfig config;
	
	private final Logger logger = LoggerFactory.getLogger(RoomMachineRegister.class);
	
	public long getRmid(){
		try{
			RMID_LOCK.lock();
			if(isFail){
				logger.error("Room Machine Config fail");
				throw new IllegalStateException("Room Machine Config fail");
			}
			return rmid;
		}finally{
			RMID_LOCK.unlock();
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
			RMID_LOCK.lock();
			this.config = config;
			if (!updateRmid().equals(RegisterState.OK)) {
				logger.error("registerRoomMachine error");
				return RegisterState.ERROR;
			}
			isFail = false;
		} finally {
			RMID_LOCK.unlock();
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
		return RegisterState.OK;
	}

}
