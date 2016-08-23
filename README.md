## [详细教程](https://mpusher.github.io/docs)

[https://mpusher.github.io/docs](https://mpusher.github.io/docs)

## 源码测试
1. ```git clone https://github.com/mpusher/mpush.git```
2. 导入到eclipse或Intellij IDEA
3. 打开```mpush-test```模块，所有的测试代码都在该模块下
4. 修改配置文件```src/test/resource/application.conf```文件修改方式参照 服务部署第6点
5. 运行```com.mpush.test.sever.ServerTestMain.java```启动长链接服务
6. 运行```com.mpush.test.client.ConnClientTestMain.java``` 模拟一个客户端
7. 运行```com.mpush.test.push.PushClientTestMain``` 模拟给用户下发消息
8. 可以在控制台观察日志看服务是否正常运行，消息是否下发成功

## 服务部署

###### 说明：mpush 服务只依赖于zookeeper和redis，当然还有JDK>=1.8

1. 安装```jdk 1.8``` 以上版本并设置```%JAVA_HOME％```

2. 安装```zookeeper``` (安装配置步骤略)

3. 安装```Redis``` (安装配置步骤略)

4. 下载mpush server正式包[https://github.com/mpusher/mpush/releases/download/0.0.1/mpush-release-0.0.1.tar.gz](https://github.com/mpusher/mpush/releases/download/0.0.1/mpush-release-0.0.1.tar.gz)

5. 解压下载的tar包`tar -zvxf mpush-release-0.0.1.tar.gz`到 mpush 目录, 结构如下

   ><pre class="md-fences">
   >drwxrwxr-x 2 shinemo shinemo  4096 Aug 20 09:30 bin —> 启动脚本
   >drwxrwxr-x 2 shinemo shinemo  4096 Aug 20 09:52 conf —> 配置文件
   >drwxrwxr-x 2 shinemo shinemo  4096 Aug 20 09:29 lib —> 核心类库
   >-rw-rw-r-- 1 shinemo shinemo 11357 May 31 11:07 LICENSE
   >drwxrwxr-x 2 shinemo shinemo  4096 Aug 20 09:32 logs —> 日志目录
   >-rw-rw-r-- 1 shinemo shinemo    21 May 31 11:07 README.md
   >drwxrwxr-x 2 shinemo shinemo  4096 Aug 20 09:52 tmp
   ></pre>

6. 修改 conf 目录下的 ```vi mpush.conf```文件, ```mpush.conf```里的配置项会覆盖同目录下的```reference.conf```文件
   ```java
      #主要修改以下配置
      mp.net.connect-server-port=3000//长链接服务对外端口, 公网端口
      mp.zk.server-address="127.0.0.1:2181"//zk 机器的地址
      mp.redis={//redis 相关配置
          #redis 集群配置，group 是个二维数组，第一层表示有多少组集群，每个集群下面可以有多台机器
          cluster-group:[
              [
                  {
                      host:"127.0.0.1"
                      port:6379
                      password:"your redis password"
                  }
              ]
          ]
      }
      //还有用于安全加密的RSA mp.security.private-key 和 mp.security.public-key 等...
   ```
    如果要修改其他配置请参照reference.conf文件

7. 给bin目录下的脚本增加执行权限```chmod u+x *.sh```

8. 执行```./mp.sh start``` 启动服务, 查看帮助```./mp.sh``` 目前支持的命令：

   ```Usage: ./mp.sh {start|start-foreground|stop|restart|status|upgrade|print-cmd}```

   ```set-env.sh``` 用于增加和修改jvm启动参数，比如堆内存、开启远程调试端口、开启jmx等

9. ```cd logs```目录，```cat mpush.out```查看服务是否启动成功 

10. 未完待续...
