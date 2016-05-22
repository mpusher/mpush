package com.mpush.tools.event;

public abstract class EventConsumer {

	public EventConsumer() {
		EventBus.I.register(this);
	}
	
}
