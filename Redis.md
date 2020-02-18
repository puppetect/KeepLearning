# Redis

## 应用场景
1. 缓存
2. 取最新N个数据的操作。如：可以将最新的50条评论的id放在list中
3. 排行榜类的应用，取topN操作。以某个条件为权重进行排序(zset)
4. 计数器的应用。生成分布式唯一主键。
5. 存储关系。比如社交关系，比如tag等(set)
6. 获取某段时间所有数据排重值，比如某段时间访问的用户id，或者是客户端ip。(set)
7. 构建队列系统。List可以构建栈和队列，zset可以构建优先级队列。
8. 实时分析系统。比如访问频率控制
9. 模拟类似于HttpSession这种需要设定过期时间的功能。
10. Pub/Sub构建实时消息系统
11. 记录日志
12. 实现分布式锁、队列、会话缓存

## spring集成
#### 依赖
*pom.xml*
```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.9.1</version>
</dependency>
<dependency>
    <groupId>org.springframeword.data</groupId>
    <artifactId>spring-data-redis</artifactId>
    <version>2.1.3.RELEASE</version>
</dependency>
```

*AppConfig.java*
```java
@Bean
public JedisConnectionFactory jedisConnectionFactory(){
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName("192.168.0.201");
    JedisConnectionFactory factory = new JedisConnectionFactory(redisStandaloneConfiguration);
    return factory;
}
@Bean
public RedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory){
    RedisTemplate redisTemplate = new RedisTemplate();
    redisTemplate.setConnectionFactory(jedisConnectionFactory);
    return redisTemplate;
}
// 或
// @Bean
// public StringRedisTemplate stringRedisTemplate(JedisConnectionFactory jedisConnectionFactory){
//     StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
//     stringRedisTemplate.setConnectionFactory(jedisConnectionFactory);
//     return stringRedisTemplate;
// }
```
*Application.java*
```java
RedisTemplate template = applicationContext.getBean(RedisTemplate.class);
template.opsForValue().get('key');
```

## 安装
#### 工具
redis-benchmark 性能测试
redis-check-aof 修复有问题的aof文件
redis-check-rdb 修复有问题的rdb文件
redis-cli 客户端操作入口
redis-sentinel 哨兵模式
redis-server 服务器启动命令

#### 基础
1. 单线程
2. 默认16个数据库，初始使用0号库
3. 密码统一管理，所有库密码相同（默认没有密码）
4. 设置外网访问: 注释bind(或改成bind 0.0.0.0)并且把protect-mode设置为no

## 使用

#### Pipeline
传统做法： n次连接时间+n次处理时间
```java
Jedis jedis = new Jedis("localhost", 6379);
for (int i=0; i<10000; i++){
    jedis.hset(i, i, i);
}
jedis.close();
```
管道优化： 1次连接时间+n次处理时间
```java
Jedis jedis = new Jedis("localhost", 6379);
Pipeline pipeline = jedis.pipelined();
for(int i=0; i<10000; i++){
    pipeline.hset(i, i, i);
}
pipeline.syncAndReturnAll();
jedis.close();
```

## 持久化

#### RDB

1. 是什么
    redis会单独fork一个与当前进程一样的子线程进行持久化，这个子线程的所有数据都和原线程一模一样，会先将数据写入一个临时文件中，等持久化结束再用这个临时文件替换上次持久化好的文件。整个过程中主线程不进行任何io操作，确保了极高的性能。

2. 持久化文件在哪里
    配置文件中指定。
    ```xml
    dbfilename dump.rdb
    dir ./
    ```

3. 他什么时候fork子线程，或者什么时候出发rdb持久化机制？
    - `SHUTDOWN`时，如果没有开启aof，就会触发。
    - 配置文件中默认的快照时间触发。
    ```xml
    save 900 1  // 900s内进行1次增删改则会触发
    save 300 10 // 300s内进行10次增删改则会触发
    save 60 10000 // 60s内进行10000次增删改则会触发
    ```
    - 执行命令save或者bgsave时触发。注意：save是同步操作，会阻塞主线程；而bgsave会在后台异步快照。
    - flushall命令，但是里面是空的，没有意义。


#### AOF

1. 是什么
    将redis的操作日志以追加的方式写入文件，读操作是不记录的
2. 持久化文件在哪里
    ```xml
    appendonly yes //开启aof
    appendfilename appendonly.aof
    dir ./
    ```
3. 触发机制
    按配置文件的设置触发。
    ```xml
    appendfsync always //同步持久化，每次发生数据变更时，立即记录到磁盘（慢，安全）
    appendfsync everysec //每秒同步一次 （默认值，很快，但可能会失去一秒以内的数据）
    appendfsync no // 等操作系统自动调用 （快，持久化没保证）
    ```

4. aof重写机制
    当aof增长到一定大小的时候，redis能够调用bgrewriteaof对日志文件进行重写。当aof文件大小的增长率大于配置项时自动开启重写
    ```xml
    auto-aof-rewrite-percentage 100 //超过原大小的100%时重写
    auto-aof-rewrite-min-size 64mb //超过64mb时重写
    ```

5. 混合持久化机制
    5.0后默认开启，之前版本需在配置文件中手动开启
    ```xml
    aof-use-rdb-preamble yes
    ```
    将aof文件以rdb格式重写。新的aof文件前半段是rdb格式的全量数据，后半段是aof格式的增量数据。它结合了二者优点，rdb的快速加载和aof的及时性

## 集群

#### redis集群演变过程
1. 单机版
核心技术：持久化。
2. 主从复制
复制是高可用的基础，实现了数据的多机备份以及对于读操作的负载均衡和简单的故障恢复。缺陷是故障恢复无法自动化；写操作无法负载均衡；存储能力受到单机的限制。
3. 哨兵
在复制的基础上，哨兵实现了自动化的故障恢复。
4. 集群
通过集群，redis解决了写操作无法负载均衡，以及存储能力受到单机限制的问题。

## 启动

1. 配置
```xml
cluster-enabled yes
cluster-config-file redis.conf
```
*\*tips:linux下快速替换配置端口*
把redis7000/redis.conf复制到redis7001/redis.conf，并将文件中所有7000都改成7001
```
sed 's/7000/7001/g' redis7000/redis.conf > redis7001/redis.conf
```

2. 搭建
启动节点
```
redis-server redis7000/redis.conf
redis-server redis7001/redis.conf
...
```
指配集群模式
```
redis-cli --cluster create host1:port1 ... hostN:portN --cluster-replicas 1
```
上一行命令相当于执行了下列三步：
```
//meet
cluster meet ip port

//指派槽
查看crc16算法算出key的槽位命令
cluster keyslot {key} (查看key的槽位)
cluster addslots slot (槽位下标)

//分配主从
cluster replicate node-id
```

3. 连接
```
redis-cli -h {hostname} -p {port} -c
// -c 指以集群方式连接，否则将仍然以单机方式连接
```
在redis-cli中对集群插入数据，如果槽位不对可以自动重定向。**但jedis中没有这个功能!**


