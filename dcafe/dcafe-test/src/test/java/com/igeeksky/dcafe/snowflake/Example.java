package com.igeeksky.dcafe.snowflake;

import com.igeeksky.dcafe.snowflake.PrimaryKeyGen.RegisterState;

/**
 * 使用示例
 * @author Tony.Lau
 */
public class Example{
	
	static {
		RoomMachineConfig config = new RoomMachineConfig(0, 1, 0, 1);
		RegisterState state = PrimaryKeyGen.INSTANCE.registerRoomMachine(config);
	}
	
	private static PrimaryKeyGen keyGen = PrimaryKeyGen.INSTANCE;
	
	public long getKey(){
		return keyGen.getIncrKey();
	}
	
	private static class RoomMachineConfig extends AbstractRMConfig{
		
		public RoomMachineConfig(){
			this.init();
			/*
			if(config.change()){
				refresh();
			}
			*/
		}
		
		public RoomMachineConfig(int roomId, int roomBitNum, int machineId, int machineBitNum) {
			super(roomId, roomBitNum, machineId, machineBitNum);
			/*
			if(config.change()){
				refresh();
			}
			*/
		}
		
		@Override
		protected RegisterState init() {
			// 获取配置并设置参数
			//this.roomId = 
			//this.roomBitNum = 
			//this.machineId = 
			//this.machineBitNum = 
			return PrimaryKeyGen.INSTANCE.registerRoomMachine(this);
		}
		
		@Override
		protected RegisterState refresh() {
			//  获取配置并更新参数
			//this.roomId = 
			//this.roomBitNum = 
			//this.machineId = 
			//this.machineBitNum = 
			return PrimaryKeyGen.INSTANCE.registerRoomMachine(this);
		}

		@Override
		public int getRoomId() {
			return roomId;
		}

		@Override
		public int getRoomBitNum() {
			return roomBitNum;
		}

		@Override
		public int getMachineId() {
			return machineId;
		}

		@Override
		public int getMachineBitNum() {
			return machineBitNum;
		}
		
		@Override
		public int getTimeUpdatePeriod(){
			return timeUpdatePeriod;
		}

	}
	
}