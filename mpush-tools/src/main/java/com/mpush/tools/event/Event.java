package com.mpush.tools.event;

public class Event {
	
	private final EventType eventType;
    private final Object source;

    public Event(final EventType eventType, final Object source) {
        this.eventType = eventType;
        this.source = source;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Object getSource() {
        return source;
    }

}
