package com.igeeksky.dcafe.keygen.snowflake;

import com.igeeksky.dcafe.keygen.snowflake.TimeGenerator.RegisterState;

/**
 * @author Tony.Lau
 * @create 2016-12-23 07:55:56
 */
public abstract class AbstractRMConfig {
	
	public AbstractRMConfig(){}
	
	public AbstractRMConfig(int roomId, int roomBitNum, int machineId, int machineBitNum, int timeUpdatePeriod){
		this.roomId = roomId;
		this.roomBitNum = roomBitNum;
		this.machineId = machineId;
		this.machineBitNum = machineBitNum;
		this.timeUpdatePeriod = timeUpdatePeriod;
	}
	
	protected int roomId = -1;

	protected int roomBitNum = -1;

	protected int machineId = -1;

	protected int machineBitNum = -1;
	
	protected int timeUpdatePeriod = 1000;
	
	/** 配置失效时停止产生主键 */
	protected RegisterState fail(){
		return TimeGenerator.INSTANCE.registerRoomMachine(new FailRMConfig());
	}
	
	/** 初始化配置 */
	protected abstract RegisterState init();
	
	/** 重新注册配置 */
	protected abstract RegisterState refresh();
	
	public abstract int getTimeUpdatePeriod();

	public abstract int getRoomId();

	public abstract int getRoomBitNum();

	public abstract int getMachineId();

	public abstract int getMachineBitNum();

}
