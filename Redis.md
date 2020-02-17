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
