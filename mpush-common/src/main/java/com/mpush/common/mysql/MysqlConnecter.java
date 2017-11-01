package com.mpush.common.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/23 0023.
 */
public class MysqlConnecter {
    /**
     * -------------
     * # if you want to connect mysql, you should go to com.teamghz.configure.MysqlConnecter.java to edit information
     * --------------
     * # insert/update -> int update(String sql) : "sql" is what you want to execute
     * # return a integer, when 0 -> false; when other(n) success and this operation affect n lines
     * --------------
     * # delete        -> int delete(String sql) : "sql" is what you want to execute
     * # return a integer, when 0 -> false; when other(n) success and this operation affect n lines
     * --------------
     * # query         -> ArrayList<Map<String, String>> select(String sql, String tableName) :
     *                                      "sql" is what you want to execute
     *                                      "tableName" is the table name which you want to operate
     * # return a ArrayList, the elements in the ArrayList is Map<String, String>
     * # every Map is one query result
     * # when you need to use the data returned:
     * ArrayList<Map<String, String>> result = mc.select("select * from User", "User");
     *  for (Map<String, String> map : result) {
     *      System.out.println("______________________");
     *      for(Map.Entry<String, String> entry:map.entrySet()){
     *            System.out.println(entry.getKey()+"--->"+entry.getValue());
     *      }
     *  }
     * --------------
     *
     */
    private Connection connection = null;
    private boolean connected = false;

    public MysqlConnecter() {
        try {
            Class.forName(Configure.DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR AT MysqlConnecter");
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(Configure.URL, Configure.USERNAME, Configure.PASSWORD);
            connected = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int insert(String sql)
    {
        int lineNum = 0;
        if (!connected) return 0;
        try{
            PreparedStatement preStmt = connection.prepareStatement(sql);
            lineNum = preStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return lineNum;
    }

    public int update(String sql)
    {
        int lineNum = 0;
        if (!connected) return 0;
        try{
            PreparedStatement preStmt = connection.prepareStatement(sql);
            lineNum = preStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return lineNum;
    }
    public ArrayList<Map<String, String>> select(String sql, String tableName)
    {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        try
        {
            Statement stmt = connection.createStatement();
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
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public int delete(String sql)
    {
        int lineNum = 0;
        try
        {
            Statement stmt = connection.createStatement();
            lineNum = stmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return lineNum;
    }
    // 获取当前表的关键字，并以字符串数组的形式返回：如“username”，“id“等
    private String[] getFrame(String tableName) {
        String[] result = new String[Configure.TABLELEN];
        try
        {
            Statement stmt = connection.createStatement();
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
        }
        return result;
    }

    /**
     * 获取单个数据
     * @param sql
     * @return
     */
    public String selectOne(String sql)
    {
        String result = null;

        try
        {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                result = rs.getString(1);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
