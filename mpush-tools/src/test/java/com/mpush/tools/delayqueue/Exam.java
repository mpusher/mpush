/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.tools.delayqueue;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 考试时间为120分钟，30分钟后才可交卷，初始化考生完成试卷时间最小应为30分钟
 * 对于能够在120分钟内交卷的考生，如何实现这些考生交卷
 * 对于120分钟内没有完成考试的考生，在120分钟考试时间到后需要让他们强制交卷
 * 在所有的考生都交完卷后，需要将控制线程关闭
 * 
 * 实现思想：用DelayQueue存储考生（Student类），每一个考生都有自己的名字和完成试卷的时间，
 * Teacher线程对DelayQueue进行监控，收取完成试卷小于120分钟的学生的试卷。
 * 当考试时间120分钟到时，先关闭Teacher线程，然后强制DelayQueue中还存在的考生交卷。
 * 每一个考生交卷都会进行一次countDownLatch.countDown()，当countDownLatch.await()不再阻塞说明所有考生都交完卷了，而后结束考试。
 * 
 */

public class Exam {

	public static void main(String[] args) throws InterruptedException {
		int studentNumber = 20;
        CountDownLatch countDownLatch = new CountDownLatch(studentNumber+1);
        DelayQueue<Student> students = new DelayQueue<Student>();
        Random random = new Random();
        for (int i = 0; i < studentNumber; i++) {
            students.put(new Student("student"+(i+1), 30+random.nextInt(120),countDownLatch));
        }
        Thread teacherThread =new Thread(new Teacher(students)); 
        students.put(new EndExam(students, 120,countDownLatch,teacherThread));
        teacherThread.start();
        countDownLatch.await();
        System.out.println(" 考试时间到，全部交卷！");  
	}
	
}

class Student implements Runnable,Delayed{
	
	private String name;
	private long workTime; //考试时间
	private long submitTime; //交卷时间
	private boolean isForce = false;
	private CountDownLatch countDownLatch;
	
	public Student() {
	}
	
	public Student(String name,long workTime,CountDownLatch countDownLatch){
        this.name = name;
        this.workTime = workTime;
        this.submitTime = TimeUnit.NANOSECONDS.convert(workTime, TimeUnit.NANOSECONDS)+System.nanoTime();
        this.countDownLatch = countDownLatch;
    }

	@Override
	public int compareTo(Delayed o) {
		if(o == null || ! (o instanceof Student)) return 1;
        if(o == this) return 0; 
        Student s = (Student)o;
        if (this.workTime > s.workTime) {
            return 1;
        }else if (this.workTime == s.workTime) {
            return 0;
        }else {
            return -1;
        }
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(submitTime - System.nanoTime(),  TimeUnit.NANOSECONDS);
	}

	@Override
	public void run() {
		if (isForce) {
            System.out.println(name + " 交卷, 希望用时" + workTime + "分钟"+" ,实际用时 120分钟" );
        }else {
            System.out.println(name + " 交卷, 希望用时" + workTime + "分钟"+" ,实际用时 "+workTime +" 分钟");  
        }
        countDownLatch.countDown();
	}
	
	public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean isForce) {
        this.isForce = isForce;
    }
	
}

class EndExam extends Student{

    private DelayQueue<Student> students;
    private CountDownLatch countDownLatch;
    private Thread teacherThread;
    
    public EndExam(DelayQueue<Student> students, long workTime, CountDownLatch countDownLatch,Thread teacherThread) {
        super("强制收卷", workTime,countDownLatch);
        this.students = students;
        this.countDownLatch = countDownLatch;
        this.teacherThread = teacherThread;
    }
    
    
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
        teacherThread.interrupt();
        Student tmpStudent;
        for (Iterator<Student> iterator2 = students.iterator(); iterator2.hasNext();) {
            tmpStudent = iterator2.next();
            tmpStudent.setForce(true);
            tmpStudent.run();
        }
        countDownLatch.countDown();
    }
    
}


class Teacher implements Runnable{
	
	private static final Logger log = LoggerFactory.getLogger(Teacher.class);
	
	private DelayQueue<Student> students;

	public Teacher(DelayQueue<Student> students) {
		this.students = students;
	}
	
	@Override
	public void run() {
		try{
			log.warn(" test start ");
			while(!Thread.interrupted()){
				students.take().run();
			}
		}catch(Exception e){
			log.warn("exception :",e);
		}
	}
}
