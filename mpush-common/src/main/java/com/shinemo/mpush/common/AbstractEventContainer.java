package com.shinemo.mpush.common;

public abstract class AbstractEventContainer {

	public AbstractEventContainer() {
		EventBus.INSTANCE.register(this);
	}
	
}
