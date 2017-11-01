package com.mpush.common.druid;

import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by hardy on 2017/9/9.
 */
public class MysqlConnecter {



    /**
     * 数据更新
     */
    public int update(String sql) {
        DBPoolConnection dbp = DBPoolConnection.getInstance();//获取数据库连接池
        DruidPooledConnection conn = null;
        PreparedStatement preStmt = null;
        int lineNum = 0;
        try {
            conn = dbp.getConnection();
            preStmt = conn.prepareStatement(sql);
            lineNum = preStmt.executeUpdate();
            preStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preStmt != null) {
                try {
                    preStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return lineNum;
    }

    /**
     * 获取单个数据
     *
     * @param sql
     * @return
     */
    public String selectOne(String sql) {
        DBPoolConnection dbp = DBPoolConnection.getInstance();//获取数据库连接池
        DruidPooledConnection conn = null;
        Statement stmt = null;
        String result = null;
        ResultSet rs = null;
        try {
            System.out.println("--------sql-----"+sql);
            conn = dbp.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                result = rs.getString(1);
            }

        } catch (SQLException e) {
           System.out.println("selectOne()异常 "+sql+"\n"+"---------"+e.getMessage());
            dbp = DBPoolConnection.getInstance();//获取数据库连接池
            try {
                conn=dbp.getConnection();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(stmt!=null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("-----查询出来的结果---result-----"+result);
        return result;
    }

}
