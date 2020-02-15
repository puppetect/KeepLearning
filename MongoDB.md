# MongoDB

## 简介

#### NoSQL
解决在web2.0时代出现的三高要求：
1. 对数据库高并发读写
2. 对海量数据的高效率存储和访问
3. 对数据库的高可扩展性和高可用性
而RDB的一些特性，在web2.0里变得不那么重要：
1. 数据库事务的一致性
2. 数据库的实时读写
3. 复杂的sql查询，特别是多表关联查询

#### CAP定理（布鲁尔定理）
对于一个分布式计算系统，不可能同时满足以下三点。
1. 强一致性(Consistency)：系统在执行过某项操作后仍然处于一致的，在分布式系统中，更新操作执行成功后所有的用户都应该读取到最新的值，这样的系统被认为有强一致性。
2. 可用性(Availability)：每一个操作总是能够在一定时间内返回结果
3. 分区容错性(Partition tolerance)：系统在存在网络分区的情况下仍然可以接收请求并处理，这里网络分区是指由于某种原因网络被分成若干个孤立区域，而区域之间互不相通。

根据CAP原理将数据库分成了一下三大类：
1. CA: 单点集群，满足一致性、可用性，通常在可扩展性上不太强大，比如RDB;
2. CP: 满足一致性和分区容错性，通常性能不是特别高，如分布式数据库;
3. AP: 满足可用性和分区容错性，通常可能对一致性要求低一些，如大多数的NoSQL

BASE(Basically Available, Soft-state, Eventual consistency)
1. 基本可用(Basically Available)： 系统能够基本运行、一直提供服务
2. 软状态(Soft-state)：系统不要求一直保持强一致状态
3. 最终一致性(Eventual consistency)：系统需要在某一时刻后达到一致性要求

#### 基本概念
**数据库**
mongodb的一个实例可以拥有一个或多个相互独立的数据库，每个数据库有自己的集合。数据库最终会成为文件，文件名就是数据库名
**集合**
集合可以看作是拥有动态模式的表
**文档**
文档是mongodb中基本的数据单元，相当于RDB中的行。
文档是键值对的一个**有序**集合。相同的键值对如果顺序不同，也是不同的文档

## Manual
**方法**
[Shell Methods](https://docs.mongodb.com/manual/reference/method/)
**增删改查**
[CRUD](https://docs.mongodb.com/manual/crud/)
**聚合框架**
采用多个构件来创建一个管道，用于对一连串的文档进行处理。包括：筛选($match)、投影($project)、分组($group)、排序($sort)、限制($limit)和跳过($skip)
[Aggregation](https://docs.mongodb.com/manual/aggregation/)
**索引**
[Index](https://docs.mongodb.com/manual/indexes/)
**副本集**
[Replica Set](https://docs.mongodb.com/manual/replication/)

