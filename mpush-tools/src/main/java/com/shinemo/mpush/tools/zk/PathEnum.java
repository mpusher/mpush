package com.shinemo.mpush.tools.zk;


public enum PathEnum {
	
	CONNECTION_SERVER_ALL_HOST("/cs/hosts","连接服务器应用注册的路径"){
		@Override
		public String getPathByIp(String ip) {
			return getPath()+"/machine";
		}
		@Override
		public String getPathByName(String name) {
			return getPath()+"/"+name;
		}
	},
	CONNECTION_SERVER_REDIS("/cs/redis","连接服务器redis注册的地方"){
		@Override
		public String getPathByIp(String ip) {
			return getPath();
		}
		@Override
		public String getPathByName(String name) {
			return getPath()+"/"+name;
		}
	},
    CONNECTION_SERVER_KICK("/cs/%s/kick/con","连接服务器踢人的路径"){
		@Override
		public String getPathByIp(String ip) {
			return String.format(getPath(), ip);
		}
		@Override
		public String getPathByName(String name) {
			return getPath()+"/"+name;
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
	
	//不同的机器，注册到不同的路径
	public abstract String getPathByIp(String ip); 
	
	//根据从zk中获取的app的值，拼装全路径
	public abstract String getPathByName(String name); 

	public static void main(String[] args) {
		String test = "/cs/%s/kick";
		
		System.out.println(String.format(test, "10.1.10.65"));
	}

}
