package com.igeeksky.dcafe.keygen;

import com.igeeksky.dcafe.keygen.TimeGenerator.RegisterState;

/**
 * <b>！！！不推荐！！！方式一：无参构造方法（允许运行期动态改变机器ID）</b><br>
 * 1. 通过zookeeper获取唯一的机器ID和机房ID。<br>
 * 2. 将机器的Mac地址和IP地址注册到存储节点。<br>
 * 3. Watch节点，当发生信息变更时更新ID信息并重新registerHolder到TimeGenerator。<br>
 * 4. 警告：如果机器时间有快有慢，极有可能发生ID重复；
 *    如果是分布式数据库，可以通过Hash取余实时存入到同一数据库，即使发生ID重复最多也就是数据保存失败；
 *    但如果是架构设计不合理，先取主键然后再通过消息中间件等异步保存数据，则绝对不能动态更新机器ID。<br><br>
 * 
 * <b>推荐！！方式二：有参构造方法（不允许运行期动态改变机器ID）</b><br>
 * 1. 通过配置文件 或者 zookeeper获取唯一的机器ID和机房ID。<br>
 * 2. 将机器的Mac地址和IP地址注册到存储节点。<br>
 * 3. 一旦发现无法连接zookeeper，立刻注册FailRMHolder到TimeGenerator,此时所有key都将返回-1；
 * 但要注意的是断开连接到注册FailRMHolder之间如果有其它机器自动上线，且机器ID与当前机器相同、且时间比当前机器慢，ID会发生重复。
 * 
 * @author Tony.Lau
 * @create 2016-12-23 07:55:56
 * @update
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
