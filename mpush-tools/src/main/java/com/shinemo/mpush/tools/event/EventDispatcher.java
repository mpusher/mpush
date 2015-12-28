package com.shinemo.mpush.tools.event;

import java.util.ArrayList;
import java.util.List;

public class EventDispatcher {

	private static final List<EventListener> listeners = new ArrayList<EventListener>();

	public static void addEventListener(EventListener listener) {
		listeners.add(listener);
	}

	public static void fireEvent(Event event) {
		for (EventListener listener : listeners) {
			listener.onEvent(event);
		}
	}
}
