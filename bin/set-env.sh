#!/usr/bin/env bash

#io.netty.leakDetection.level
#netty的内存泄露检测分为四级：
#DISABLED: 不进行内存泄露的检测；
#SIMPLE: 抽样检测，且只对部分方法调用进行记录，消耗较小，有泄漏时可能会延迟报告，默认级别；
#ADVANCED: 抽样检测，记录对象最近几次的调用记录，有泄漏时可能会延迟报告；
#PARANOID: 每次创建一个对象时都进行泄露检测，且会记录对象最近的详细调用记录。是比较激进的内存泄露检测级别，消耗最大，建议只在测试时使用。

#Dio.netty.noKeySetOptimization
#是否禁用nio Selector.selectedKeys优化, 通过反射实现, 默认false

JVM_FLAGS="-Dio.netty.leakDetection.level=advanced -Dio.netty.noKeySetOptimization=false"

#开启远程调试
#JVM_FLAGS="$JVM_FLAGS -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8008"