package com.shinemo.mpush.api;

public interface Client {

	public void close(final String cause);

    public String toString();

    public boolean isEnabled();

    public boolean isConnected();
    
    public void resetHbTimes();

    public int inceaseAndGetHbTimes();
    
    public void startHeartBeat() throws Exception;
    
    /**
     * host:port
     */
    public String getUrl();
    
    public String getRemoteHost();
    
    public int getRemotePort();
	
}
