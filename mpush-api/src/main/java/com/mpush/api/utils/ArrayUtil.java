package com.mpush.api.utils;

import java.util.Arrays;

/**
 * @description:
 * @author: dengliaoyan
 * @create: 2019-06-15 08:36
 **/
public class ArrayUtil {

    /**
     * 将指定信息添加到数组中
     * @param src
     * @param add
     * @return
     */
    public static String[] addArr(String[] src, String add){
        boolean has = false;
        if(src != null && src.length>0){
            for(String item : src){
                if(item.equals(add)){
                    has = true;
                    break;
                }
            }
        }
        if(!has){
            String[] newArr = Arrays.copyOf(src, src.length+1);
            newArr[src.length] = add;
            return newArr;
        }
        return src;
    }

    /**
     * 从数组中删除指定信息
     * @param src
     * @param remove
     * @return
     */
    public static String[] removeArr(String[] src, String remove){
        boolean has = false;
        int index = -1;
        if(src != null && src.length>0){
            for(int i=0; i<src.length; i++){
                if(src[i].equals(remove)){
                    index = i;
                    has = true;
                    break;
                }
            }
        }
        if(has){
            String[] newSrc = new String[src.length-1];
            for(int i=0,j=-1; i<newSrc.length; i++,j++){
                if(j == index){
                    j++;
                }
                newSrc[i] = src[j];
            }
            return newSrc;
        }
        return src;
    }
}
