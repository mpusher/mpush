package com.mpush.common.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * mysql数据连接类
 * @author hongjun
 * @date 2017-09-09
 *
 */
public class MysqlDataConnecter {
	private Connection conn = null;
    private boolean connected = false;
    // 声明DBManager的私有对象db  
    private static MysqlDataConnecter datasource=new MysqlDataConnecter() ;  
    /** 
     * 创建私有构造函数 
     */  
    private MysqlDataConnecter() {  
        try {  
            // 加载驱动  
        	 Class.forName(Configure.DRIVER);
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();  
        }  
    }  
      
   
    /** 
     * 提供一个静态方法 
     * @return 返回本类的实例 
     */  
    public static synchronized MysqlDataConnecter getInstance() {  
    	 if(datasource == null){  
             synchronized (MysqlDataConnecter.class){  
                 if(datasource == null){  
                	 datasource = new MysqlDataConnecter();  
                 }  
             }  
         }  
         return datasource; 
    }  
    
    /** 
     * 获取连接 
     * @return conn 
     */  
    public Connection getConnection() {  
        try {  
            // 获取连接  
        	conn = DriverManager.getConnection(Configure.URL, Configure.USERNAME, Configure.PASSWORD);
             connected = true;
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
        return conn;  
    }  
  

	/**
	 * 数据插入
	 */
    public int insert(String sql) throws SQLException {
        int lineNum = 0;
        if (conn == null || conn.isClosed()) {
			conn = datasource.getConnection();
		}
        try{
            PreparedStatement preStmt = conn.prepareStatement(sql);
            lineNum = preStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
        return lineNum;
    }

    /**
     * 数据更新
     */
    public int update(String sql) throws SQLException {
        int lineNum = 0;
        if (conn == null || conn.isClosed()) {
			conn = datasource.getConnection();
		}
        try{
            PreparedStatement preStmt = conn.prepareStatement(sql);
            lineNum = preStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
        return lineNum;
    }
    
    
    public ArrayList<Map<String, String>> select(String sql, String tableName) throws SQLException {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        if (conn == null || conn.isClosed()) {
			conn = datasource.getConnection();
		}
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            String[] frame = getFrame(tableName);
            while (rs.next())
            {
                Map<String, String> tmp = new HashMap<>();
                for (String key : frame) {
                    if (key == "#") break;
                    tmp.put(key, rs.getString(key));
                }
                result.add(tmp);
            }
            rs.close();
            rs=null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
        return result;
    }
    
    /**
     *数据删除 
     */
    public int delete(String sql) throws SQLException {
        int lineNum = 0;
        if (conn == null || conn.isClosed()) {
     			conn = datasource.getConnection();
     		}
        try
        {
            Statement stmt = conn.createStatement();
            lineNum = stmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
        return lineNum;
    }
    // 获取当前表的关键字，并以字符串数组的形式返回：如“username”，“id“等
    private String[] getFrame(String tableName) throws SQLException {
        String[] result = new String[Configure.TABLELEN];
        if (conn == null || conn.isClosed()) {
 			conn = datasource.getConnection();
 		}
        try
        {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("show columns from " + tableName);
            int i = 0;
            while (rs.next())
            {
                result[i++] = rs.getString(1);
            }
            result[i] = "#";
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
        return result;
    }

    /**
     * 获取单个数据
     * @param sql
     * @return
     */
    public String selectOne(String sql) throws SQLException {
        String result = null;
        if (conn == null || conn.isClosed()) {
 			conn = datasource.getConnection();
 		}
        try
        {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                result = rs.getString(1);
            }
            rs.close();
            rs=null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
        return result;
    }
}
