package com.shinemo.mpush.tools;

import java.util.Random;

public class RandomUtil {

	public static int random(int total){
		Random ran = new Random();
		return ran.nextInt(total);
	}
	
	public static void main(String[] args) {
		
		for(int i = 0;i<20;i++){
			System.out.println(random(3));
		}
		
	}
	
}
