package com.mpush.tools;

/**
 * @description:
 * @author: dengliaoyan
 * @create: 2019-06-14 11:48
 **/
public class StringUtil {
    /**
     * 用于验证用户id、别名、标签的正则表达式
     */
    private static final String REGEX_USERID_ALIAS_TAGS = "^([A-Za-z0-9_\\-]){1,127}$";
    public static boolean verifyString(String str, String regex){
        return str.matches(regex);
    }
    public static boolean verifyUserId(String userId){
        if(userId==null){
            return false;
        }
        return verifyString(userId, REGEX_USERID_ALIAS_TAGS);
    }
    public static boolean verifyAlias(String alias){
        if(alias==null){
            return false;
        }
        return verifyString(alias, REGEX_USERID_ALIAS_TAGS);
    }
    public static boolean verifyTags(String tags){
        if(tags==null){
            return false;
        }
        if(tags.indexOf(",")>0){
            String[] tagsArr = tags.split(",");
            boolean rev = true;
            for(String item : tagsArr){
                rev = rev && verifyString(item, REGEX_USERID_ALIAS_TAGS);
                if(!rev){
                    return rev;
                }
            }
            return rev;
        }else{
            return verifyString(tags, REGEX_USERID_ALIAS_TAGS);
        }
    }
}
