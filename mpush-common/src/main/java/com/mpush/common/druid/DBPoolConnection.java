package com.mpush.common.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.mpush.tools.config.CC;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 要实现单例模式，保证全局只有一个数据库连接池
 *
 * @author hongjun
 * @date 2017-09-09
 */
public class DBPoolConnection {
    static Logger log = Logger.getLogger(DBPoolConnection.class);
    private static DBPoolConnection dbPoolConnection = null;
    private static DruidDataSource druidDataSource = null;

    static {
//        Properties properties = loadPropertiesFile("classes/db_server.properties");
        Properties properties = new Properties();
        properties.put("driverClassName", CC.mp.druid.driverClassName);
        properties.put("url", CC.mp.druid.url);
        properties.put("username", CC.mp.druid.username);
        properties.put("password", CC.mp.druid.password);
        properties.put("maxActive", CC.mp.druid.maxActive);

        try {
            druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties); //DruidDataSrouce工厂模式
        } catch (Exception e) {
            log.error("获取配置失败");
        }
    }

    /**
     * 数据库连接池单例
     *
     * @return
     */
    public static synchronized DBPoolConnection getInstance() {
        if (null == dbPoolConnection) {
            dbPoolConnection = new DBPoolConnection();
        }
        return dbPoolConnection;
    }

    /**
     * 返回druid数据库连接
     *
     * @return
     * @throws SQLException
     */
    public DruidPooledConnection getConnection() throws SQLException {
        return druidDataSource.getConnection();
    }

    /**
     * @param string 配置文件名
     * @return Properties对象
     */
    private static Properties loadPropertiesFile(String fullFile) {
        String webRootPath = null;
        if (null == fullFile || fullFile.equals("")) {
            throw new IllegalArgumentException("Properties file path can not be null" + fullFile);
        }
        webRootPath = DBPoolConnection.class.getClassLoader().getResource("").getPath();
        webRootPath = new File(webRootPath).getParent();
        InputStream inputStream = null;
        Properties p = null;
        try {
            inputStream = new FileInputStream(new File(webRootPath + File.separator + fullFile));
            p = new Properties();
            p.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return p;
    }

}