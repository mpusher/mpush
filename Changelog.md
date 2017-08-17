#### v0.8.0

1. 增加本地ip和外网ip配置项
2. ConnServer和GatewayServer增加bind ip和register ip 配置项
3. ConnServer增加权重等扩展属性配置项
4. 系统模块化重构：SPI增加Plugin接口及其init方法，增加MPushContext对象，方便插件初始化时控制系统内部对象
5. 广播推送增加RedisBroadcastController存储推送结果到redis, 并通过redis控制推送任务
6. 启动脚本优化修复不能加载自定义SPI的bug
7. EventBus订阅方法增加@AllowConcurrentEvents注解，提高高并发性能
8. 代码优化，当GatewayClient启动时从ZK获取的链接为空时，后续会尝试重新获取
9. 优化ServerLauncher和PushClient代码，方便自定义系统配置和spring方式启动
10. 依赖类库升级，日志优化，及其他bug fix




#### v0.7.1

1. 修复网关客户端获取连接失败bug
2. 修复ZK临时节点在连接断开未重新注册bug
3. PushClient代码优化，依赖服务启动／停止顺序优化
4. 增加查询在线用户列表接口，修复Json转换bug
5. 修改http代理request.copy引用计数bug
6. 依赖类库升级，日志优化，及其他bug fix




#### v0.7.0

1. 网关新增udp, sctp协议支持，目前支持的协议有tcp/udp/sctp/udt, 推荐tcp
2. 增加websocket接入层，和原接入层共享线程资源，增加websocket js client
3. 抽象出cache层，不再直接依赖redis模块，支持自定义缓存实现
4. 抽象出服务注册与发现层，不再直接依赖Zookeeper模块, 支持自定义服务注册发现
5. 添加测试用的默认缓存实现以及注册发现实现，在不安装redis,zk的情况下也能进行源码测试
6. 推送中心模块重构，支持不同的消息来源，支持自定义消息来源，编写从MQ订阅消息demo
7. Gateway Client代码重构，支持多线程，多连接数配置
8. 线程池优化，重新设计各模块线程配置方式，EventBus使用动态线程池，增加Netty线程池监控
9. PushClient任务超时代码优化, 优化Timer任务线程池，在任务取消后直接从队列里删除
10. PushSender同步调用直接返回PushResult不再兼容老的返回Boolean类型
11. 修改TimeLine多线程bug，优化PushRequest多线程下代码 
12. 修复ID_SEQ在高并发下重复的问题，不再使用LongAdder
13. 代码优化，内存优化，修复推送超时问题
14. 增加推送压测代码，增加推送统计及流控QPS监控等
15. 增加tcp/udp 发送接收缓冲区配置
16. 增netty write-buffer-water-mark配置
17. 代码优化, 内存优化，及时释放无用的对象
18. 流控调优，默认关闭流量整形
19. 增加jmx监控统计, 脚本加入JMX设置，jvm设置
20. 增加PushCenter消息流转时间线, 方便监控消息的各个生命周期的耗时(PushClient -> GatewayClient -> GatewayServer -> PushCenter -> ConnServer -> Client)
21. 服务启动/停止流程优化，boot chain正序启动，逆序停止，启动流程日志优化




#### v0.6.1

1. 产品版本策略修改，主版本前移一位，最后一位用于小版本bug fix
2. 新增支持单机多实例部署方式
3. 升级依赖类库，解决由于版本升级引起的jedis和zk兼容性问题
4. 核心日志打印内容优化，更利于问题排查
5. 修复connId高并发下可能重复的bug
6. 增加压测代码，优化测试模块
7. 配置文件优化，增加相应的注释说明
8. 修复流控发送计数引起的bug
9. 优化内存占用，连接断开后立即释放Connection的引用
10. 其他bug fix及代码优化




#### v0.0.6

1. 网关服务增加UDP及组播支持，后续网关部分的TCP部分将逐步淘汰
2. 新增用户打标，修改标签，取消标签功能
3. 全网推送增加按标签过滤，按条件过滤，条件表达式目前支持javascript
4. Service模块代码优化，增加同步启动/停止，超时监控
5. 推送模块增加流控功能, 分为全局流控和广播流控，以及基于Redis实现的实时流控
6. 由于网关采用了UDP，PushClient模块和踢人模块增加UDP支持
7. 线程池代码优化，线程命名调整, 支持配置调整Netty boss和work的线程数
8. 路由模块:客户端定义增加SPI支持, 用户可自定义控制多端在线策略
9. 日志及配置项优化，增加mp.home配置项
10. 心跳检测优化，连接一建立就开始检测心跳，防止客户端握手失败或未握手的情况




#### v0.0.5

1. redis 增加redis3.x集群支持， 配置项不再兼容
2. 绑定用户调整，校验重复绑定，以及未解绑，就绑定的场景
3. 新增client push上行功能, 并支持用户以SPI方式集成自己的Handler
4. 全网推送增加按标签过滤推送用户
5. 增加流量控制，tcp发送缓冲区检测代码优化
6. 修复ACK超时方法调用错误bug，增加ack超时时间设置
7. 解码优化, 不再抛出解码异常，取消循环解码
8. NettyServer 增加IoRate设置，优雅停止流程优化，先关闭main reactor
9. 心跳优化，连接建立后就开始计算心跳
10. sessionId生成器性能优化，采用jdk8 LongAdder
11. Service模块开始使用java8 CompletableFuture
12. SPI模块优化增加@Spi注解，多个实现可以指定顺序
13. Profile 性能分析模块优化，增加性能监控开关配置，加入javassist优化性能
14. zk client 代码优化，修改临时节点重复注册问题，增加DNS ZK Node
15. 脚步修改start-foreground不能加载配置项bug, 修改windows启动命令bug
16. 其他bug fix




#### v0.0.4

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
