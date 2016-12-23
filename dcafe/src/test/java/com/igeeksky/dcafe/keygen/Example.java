package com.igeeksky.dcafe.keygen;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.igeeksky.dcafe.keygen.TimeGenerator.RegisterState;

/**
 * @author Tony.Lau
 * @create 2016-12-23 23:08:55
 */
public class Example{
	
	static {
		
		//1. 读取配置，获取机房ID 和 机器ID
		
		//2. 构造RMHolder对象
		RoomMachineConfig config = new RoomMachineConfig(0, 1, 0, 1, 1000);
		
		//3. 注册RMHolder对象并判断返回值
		RegisterState state = TimeGenerator.INSTANCE.registerRoomMachine(config);
	}
	
	private static PrimaryKeyGen keyGen = new PrimaryKeyGen();
	
	private static ConcurrentHashMap<Long, Integer> map = new ConcurrentHashMap<Long, Integer>(10000000);
	
	private static AtomicInteger repeat = new AtomicInteger();
	private static AtomicInteger errorNum = new AtomicInteger();
	
	static CountDownLatch latch = new CountDownLatch(2);
	
	public static void main(String[] args) {
		
		long t1 = System.currentTimeMillis();
		ScheduledExecutorService es = Executors.newScheduledThreadPool(2);
		
		try {
			es.execute(new TestKeyGen());
			es.execute(new TestKeyGen());
			latch.await();
			System.out.println("repeat----" + repeat.get());
			System.out.println("errorNum----" + errorNum.get());
			System.out.println(System.currentTimeMillis() - t1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private static class TestKeyGen implements Runnable{

		@Override
		public void run() {
			long Threadid = Thread.currentThread().getId();
			for(int i=0; i < 4096000; i++){
				Long time = keyGen.getIncrKey();
				if(time <= 0){
					errorNum.incrementAndGet();
					continue;
				}
				if(!map.containsKey(time)){
					map.put(time, i);
				}else{
					System.out.println(i);
					System.out.println(Threadid);
					System.out.println(map.get(time));
					System.out.println(Long.toBinaryString(time));
					repeat.incrementAndGet();
				}
			}
			latch.countDown();
		}
		
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
		
		public RoomMachineConfig(int roomId, int roomBitNum, int machineId, int machineBitNum, int timeUpdatePeriod) {
			super(roomId, roomBitNum, machineId, machineBitNum, timeUpdatePeriod);
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
			return TimeGenerator.INSTANCE.registerRoomMachine(this);
		}
		
		@Override
		protected RegisterState refresh() {
			//  获取配置并更新参数
			//this.roomId = 
			//this.roomBitNum = 
			//this.machineId = 
			//this.machineBitNum = 
			return TimeGenerator.INSTANCE.registerRoomMachine(this);
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