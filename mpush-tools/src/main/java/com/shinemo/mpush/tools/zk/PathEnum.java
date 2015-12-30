package com.shinemo.mpush.tools.zk;


public enum PathEnum {
	
	CONNECTION_SERVER_ALL_HOST("/cs/allhost/machine","连接服务器应用注册的路径"){
		@Override
		public String getPathByIp(String ip) {
			return getPath();
		}
	},
    CONNECTION_SERVER_KICK("/cs/%s/kick/con","连接服务器踢人的路径"){
		@Override
		public String getPathByIp(String ip) {
			return String.format(getPath(), ip);
		}
	};

	PathEnum(String path, String desc) {
		this.path = path;
		this.desc = desc;
    }

    private final String path;
    private final String desc;
    
    public String getPath() {
		return path;
	}

	public String getDesc() {
		return desc;
	}
	
	public abstract String getPathByIp(String ip); 

	public static void main(String[] args) {
		String test = "/cs/%s/kick";
		
		System.out.println(String.format(test, "10.1.10.65"));
	}

}
