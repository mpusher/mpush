package com.shinemo.mpush.cs;

public class Main {
	
	public static void main(String[] args) {
		final ConnectionServerMain connectionServerMain = new ConnectionServerMain();
		connectionServerMain.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	connectionServerMain.stop();
            }
        });
	}

}
