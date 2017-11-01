package com.mpush.common.druid;

import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestDb {
	static Logger log = Logger.getLogger(TestDb.class);
	/**
	 * 数据库连接测试
	 */
	public void connDb(){
		DBPoolConnection dbp= DBPoolConnection.getInstance();//获取数据库连接池
		DruidPooledConnection conn=null;
		PreparedStatement ptmt=null;
		try{
			conn=dbp.getConnection();//从数据库连接池中获取连接
			String sql="select * from m_user";
			ptmt=conn.prepareStatement(sql);
			ResultSet rs=ptmt.executeQuery();
			while(rs.next()){
				System.out.println(rs.getString("mobile"));
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(ptmt!=null){
				try {
					ptmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(conn!=null){
					try {
						conn.close();
						log.info("conn关闭数据库连接成功");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						log.info("conn关闭数据库连接失败"+e);
					}
				}
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestDb db=new TestDb();
		db.connDb();
	}

}
