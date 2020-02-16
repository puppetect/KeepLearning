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
[Aggregation](https://docs.mongodb.com/manual/aggregation/)

采用多个构件来创建一个管道，用于对一连串的文档进行处理。包括：筛选($match)、投影($project)、分组($group)、排序($sort)、限制($limit)和跳过($skip)


**索引**
[Index](https://docs.mongodb.com/manual/indexes/)

**副本集**
[Replica Set](https://docs.mongodb.com/manual/replication/)

## MongoDB Java Driver

#### 基本类
- MongoClient
- MongoDatabase
- MongoCollection
- Document
- Bson

#### Spring Boot

**创建连接**

*src/main/resources/application.properties*
```properties
spring.mongodb.uri=mongodb+srv://m220student:m220password@mflix-u1zmj.mongodb.net/test?retryWrites=true&w=majority
spring.mongodb.database=sample_mflix
```

*src/main/java/mflix/config/MongoDBConfiguration.java*
```java
@Configuration
@Service
public class MongoDBConfiguration {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MongoClient mongoClient(@Value("${spring.mongodb.uri}") String connectionString) {

        ConnectionString connString = new ConnectionString(connectionString);

        //TODO> Ticket: Handling Timeouts - configure the expected
        // WriteConcern `wtimeout` and `connectTimeoutMS` values
        MongoClient mongoClient = MongoClients.create(connectionString);

        return mongoClient;
    }
}
```

*src/main/java/mflix/api/daos/AbstractMFlixDao.java*
```java
@Configuration
public abstract class AbstractMFlixDao {

    protected final String MFLIX_DATABASE;
    protected MongoDatabase db;
    protected MongoClient mongoClient;
    @Value("${spring.mongodb.uri}")
    private String connectionString;

    protected AbstractMFlixDao(MongoClient mongoClient, String databaseName) {
        this.mongoClient = mongoClient;
        MFLIX_DATABASE = databaseName;
        this.db = this.mongoClient.getDatabase(MFLIX_DATABASE);
    }
}
```

*src/main/java/mflix/api/daos/UserDao.java*
```java
@Configuration
public class UserDao extends AbstractMFlixDao {

    private final MongoCollection<User> usersCollection;
    //TODO> Ticket: User Management - do the necessary changes so that the sessions collection
    //returns a Session object
    private final MongoCollection<Document> sessionsCollection;

    private final Logger log;

    @Autowired
    public UserDao(
            MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
        super(mongoClient, databaseName);
        CodecRegistry pojoCodecRegistry =
                fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
        log = LoggerFactory.getLogger(this.getClass());
        //TODO> Ticket: User Management - implement the necessary changes so that the sessions
        // collection returns a Session objects instead of Document objects.
        sessionsCollection = db.getCollection("sessions");
    }

    // ...
}
```

**Query Builders**
[Docs](http://mongodb.github.io/mongo-java-driver/3.8/builders/)

- Filters (`import static com.mongodb.client.model.Filters.*;`)
- Projections （`import static com.mongodb.client.model.Projections.*;`)
- Sorts (`import static com.mongodb.client.model.Sorts.*;`)
- Aggregation (`import static com.mongodb.client.model.Aggregates.*;`)
- Updates (`import static com.mongodb.client.model.Updates.*;`)
- Indexes (`import static com.mongodb.client.model.Indexes.*;`)

例：
```java
Bson queryFilter = eq("cast", "Salma Hayek");
Document result = moviesCollection
        .find(queryFilter)
        .sort(ascending("year"))
        .limit(1)
        .projection(fields(include("title", "year")))
        .iterator();
        .tryNext();
```

等同于：
```java
Document queryFilter = new Document("cast", "Salma Hayek");
MongoCursor result = moviesCollection
        .find(queryFilter)
        .sort(Sorts.ascending("year"))
        .limit(1)
        .projection(new Document("title", 1).append("year", 1))
        .iterator();
Document result = cursor.tryNext();
```
*\* Field projection is not performed in the MongoDB Java Driver find method.*

等同于：
```
db.movies.aggregate([{
    $match: {
        "cast": "Salma Hayek"
    }
}, {
    $sort: {
        "year": 1
    }
}, {
    $limit: 1
}, {
    $project: {
        "title": 1,
        "year": 1
    }
}])
```

等同于：
```
db.movied.find({"cast": "Salma Hayek"}, {"title":1, "year": 1}).sort({"year": 1}).limit(1)
```

** Using POJO **
[Pojo Docs](http://mongodb.github.io/mongo-java-driver/3.6/driver/getting-started/quick-start-pojo/)
[Codecs Tutorial](http://mongodb.github.io/mongo-java-driver/3.2/bson/codecs/)

- POJO和BSON之间的转换可以通过自定义Codec实现
- POJO和BSON属性字段可通过注解`@BsonProperty`关联
- `_id`会自动生成
- POJO和BSON不同类型的相同字段可以通过两种方式转化：
1. Using a POJO in conjunction with a Custom Codec
2. Using a POJO with a Default Codec and a custom field type conversion script

```java
public class UserDao extends AbstractMFlixDao {

    private final MongoCollection<User> usersCollection;

    private final Logger log;

    @Autowired
    public UserDao(
            MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
        super(mongoClient, databaseName);
        CodecRegistry pojoCodecRegistry =
                fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
        log = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Inserts the `user` object in the `users` collection.
     *
     * @param user - User object to be added
     * @return True if successful, throw IncorrectDaoOperation otherwise
     */
    public boolean addUser(User user) {
        //TODO > Ticket: Durable Writes -  you might want to use a more durable write concern here!
        usersCollection.insertOne(user);
        return true;
        //TODO > Ticket: Handling Errors - make sure to only add new users
        // and not users that already exist.

    }

```

** Cursor Methos and Aggregation Equivalents
- Cursor methods have equivalent aggregation stages
- The order by which cursor methods are appended to the find iterable does not impact the results
- **The order by which aggregation stages are defined in the pipeline does!**

**`writeConcern`**
\{w:1\}
- Only requests an acknowledgement that **one** node applied the write
- This is the default writeConcern in MongoDB

\{w:'majority'\}
- Requests acknowledgement that a **majority of nodes** in the replica set applied the write
- Takes longer than `w: 1`. (But there's no additional load on the server, so the primary can still perform the same number of writes per second)
- Is more durable than `w: 1`. Useful for ensuring vital writes are majority-committed, because a write will not be rolled back during fail over.

\{w: 0\}
- Does **not** request an acknowledgement that any nodes applied the write ("Fire-and-forget")
- Fastest writeConcern level
- Least durable writeConcern
