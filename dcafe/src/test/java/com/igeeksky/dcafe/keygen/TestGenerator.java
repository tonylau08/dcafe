package com.igeeksky.dcafe.keygen;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.igeeksky.dcafe.keygen.snowflake.AbstractRMConfig;
import com.igeeksky.dcafe.keygen.snowflake.FailRMConfig;
import com.igeeksky.dcafe.keygen.snowflake.PrimaryKeyGen;
import com.igeeksky.dcafe.keygen.snowflake.TimeGenerator;
import com.igeeksky.dcafe.keygen.snowflake.TimeGenerator.RegisterState;

/**
 * 测试类
 * @author Tony.Lau
 * @create 2016-12-23 23:08:55
 * @update 2016-12-26 20:13:24
 */
public class TestGenerator{
	
	static {
		
		//1. 读取配置，获取机房ID 和 机器ID
		
		//2. 构造RMHolder对象
		RoomMachineConfig config = new RoomMachineConfig(0, 1, 0, 1, 1000);
		
		//3. 注册RMHolder对象并判断返回值
		RegisterState state = TimeGenerator.INSTANCE.registerRoomMachine(config);
	}
	
	private static PrimaryKeyGen keyGen = PrimaryKeyGen.INSTANCE;
	
	private static ConcurrentHashMap<Long, Integer> map = new ConcurrentHashMap<Long, Integer>(10000000);
	
	private static AtomicInteger repeat = new AtomicInteger();
	private static AtomicInteger errorNum = new AtomicInteger();
	
	static CountDownLatch latch = new CountDownLatch(2);
	
	private static volatile boolean isFail = false;
	
	public static void main(String[] args) {
		
		long t1 = System.currentTimeMillis();
		ScheduledExecutorService es = Executors.newScheduledThreadPool(2);
		
		try {
			new Thread(new FailTest()).start();
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
			for(int i=0; i < 409600; i++){
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
				if(isFail){
					TimeGenerator.INSTANCE.registerRoomMachine(new RoomMachineConfig(0, 1, 0, 1, 1000));
					isFail = false;
				}
			}
			latch.countDown();
		}
		
	}
	
	private static class FailTest implements Runnable{
		
		@Override
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			TimeGenerator.INSTANCE.registerRoomMachine(new FailRMConfig());
			isFail = true;
			run();
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