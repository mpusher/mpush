package com.mpush.common.mysql;

/**
 * Created by Administrator on 2017/8/23 0023.
 */
public class Test {
    public static void main(String[] args) {
        MysqlConnecter mc = new MysqlConnecter();
        // insert
//        mc.update("insert into user(username) values(\"user5\")");
        // update
//        System.out.println(mc.update("update user set status=1 where username=\"user1\""));
        // delete
//        System.out.println(mc.delete("delete from User where userid=3"));
        // select
//        ArrayList<Map<String, String>> result = mc.select("select * from user", "user");
//        // map的遍历方法
//        for (Map<String, String> map : result) {
//            System.out.println("______________________");
//            for (Map.Entry<String, String> entry : map.entrySet()) {
//                System.out.println(entry.getKey() + "--->" + entry.getValue());
//            }
//        }

//        String result = mc.selectOne("select user_id from m_user where device_id=\"54545\"");
//        System.out.println(result);
        DateUtils dateUtils = new DateUtils();
        String now = dateUtils.getNow(dateUtils.FORMAT_LONG);
        System.out.println(mc.update("update m_user_online_time set create_time=\""+now+"\" where create_time=\"0000-00-00 00:00:00\""));
    }
}
