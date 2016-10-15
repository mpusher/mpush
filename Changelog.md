#### v0.0.5:

1. zk client 代码优化，修改临时节点重复注册问题，增加DNS ZK Node
2. 绑定用户调整，校验重复绑定，以及未解绑，就绑定的场景
3. 脚步修改start-foreground不能加载配置项bug, 修改windows启动命令bug
4. 修复ACK超时方法调用错误bug，增加ack超时时间设置
5. redis 增加redis3.x集群支持
6. 解码优化, 不再抛出解码异常，取消循环解码
7. Netty Server 增加IoRate设置，优雅停止流程优化，先关闭main reactor
8. 心跳优化，连接建立后就开始计算心跳
9. sessionId生成器性能优化，采用jdk8 LongAdder
10. Service模块开始使用java8 CompletableFuture
11. 全网推送增加按标签过滤推送用户
12. 增加流量控制，tcp发送缓冲区检测代码优化
13. 新增client push上行功能, 并支持用户以SPI方式集成自己的Handler
14. SPI模块优化增加@Spi注解，多个实现可以指定顺序
15. Profile 性能分析模块优化，增加性能监控开关配置，加入javassist优化性能
16. 其他bug fix




#### v0.0.4:

1. push client API 调整
2. push 接口增加了全网推送功能
3. 用户下线后路由信息不再删除，而是修改为下线状态
4. 修复ZK Client临时节点断开后，不能恢复注册的bug
5. 其他bug fix

#### v0.0.3

1. 增加了消息ACK功能
2. 修复脚本换行问题
3. bug fix

### v0.0.2

1. 增加多端同时在线攻能
