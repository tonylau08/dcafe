package com.igeeksky.dcafe.snowflake;

import com.igeeksky.dcafe.snowflake.PrimaryKeyGen.RegisterState;

public final class FailRMConfig extends AbstractRMConfig {
	
	@Override
	public int getRoomId() {
		return -1;
	}

	@Override
	public int getRoomBitNum() {
		return -1;
	}

	@Override
	public int getMachineId() {
		return -1;
	}

	@Override
	public int getMachineBitNum() {
		return -1;
	}

	@Override
	protected RegisterState refresh() {
		return null;
	}

	@Override
	protected RegisterState init() {
		return null;
	}

	@Override
	public int getTimeUpdatePeriod() {
		return timeUpdatePeriod;
	}

}
