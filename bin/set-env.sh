#!/usr/bin/env bash
#1. Netty 相关关设置项

#-Dio.netty.leakDetection.level
#netty的内存泄露检测分为四级：
#DISABLED: 不进行内存泄露的检测；
#SIMPLE: 抽样检测，且只对部分方法调用进行记录，消耗较小，有泄漏时可能会延迟报告，默认级别；
#ADVANCED: 抽样检测，记录对象最近几次的调用记录，有泄漏时可能会延迟报告；
#PARANOID: 每次创建一个对象时都进行泄露检测，且会记录对象最近的详细调用记录。是比较激进的内存泄露检测级别，消耗最大，建议只在测试时使用。

#-Dio.netty.selectorAutoRebuildThreshold=512 默认512
#在NIO中通过Selector的轮询当前是否有IO事件，根据JDK NIO api描述，Selector的select方法会一直阻塞，直到IO事件达到或超时，但是在Linux平台上这里有时会出现问题，在某些场景下select方法会直接返回，即使没有超时并且也没有IO事件到达，这就是著名的epoll bug，这是一个比较严重的bug，它会导致线程陷入死循环，会让CPU飙到100%，极大地影响系统的可靠性，到目前为止，JDK都没有完全解决这个问题。
#但是Netty有效的规避了这个问题，经过实践证明，epoll bug已Netty框架解决，Netty的处理方式是这样的：
#记录select空转的次数，定义一个阀值，这个阀值默认是512，可以在应用层通过设置系统属性io.netty.selectorAutoRebuildThreshold传入，当空转的次数超过了这个阀值，重新构建新Selector，将老Selector上注册的Channel转移到新建的Selector上，关闭老Selector，用新的Selector代替老Selector，详细实现可以查看NioEventLoop中的selector和rebuildSelector方法：

#-Dio.netty.noKeySetOptimization
#是否禁用nio Selector.selectedKeys优化, 通过反射实现, 默认false

JVM_FLAGS="-Dio.netty.leakDetection.level=advanced"

#JMX

JMXDISABLE=true
#JMXPORT=1099

#3. 开启远程调试

#JVM_FLAGS="$JVM_FLAGS -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8008"

#4. GC配置

#运行模式 整个堆内存大小 GC算法
#JVM_FLAGS="$JVM_FLAGS -server -Xmx1024m -Xms1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
#GC日志 发生OOM时创建堆内存转储文件
#JVM_FLAGS="$JVM_FLAGS -Xloggc:$MP_LOG_DIR/gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps"
#发生OOM后的操作
#JVM_FLAGS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$MP_LOG_DIR -XX:OnOutOfMemoryError=$MP_BIN_DIR/restart.sh"