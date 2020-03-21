# JVM

## 基本结构

JVM由三个主要的子系统构成
- 类加载子系统
- 运行时数据区（内存结构）
- 执行引擎

运行时数据区
线程共有：
    方法区(Method Area) 静态变量、常量、类信息（构造方法/接口定义）、运行时常量池
    堆(Heap) 存放对象实例
线程私有：
    虚拟机栈：栈帧
    本地方法栈
    程序计数器

栈帧(一个栈帧对应一个方法)
1. 局部变量表（存储方法局部变量和引用类型）
2. 操作数栈（所有操作都在此执行）
3. 动态链接 （把符号引用转化成直接引用）
4. 方法出口（指向方法调用者的内存地址）

## 调优工具

Jinfo 查看正在运行的java程序的扩展参数和系统属性
查看jvm的参数
```
jinfo -flags 进程id
```
查看java系统属性  等同于System.getProperties()
```
jinfo -syspros 进程id
```

Jstat 查看堆内存各部分的使用量，以及加载类的数量。
```
jstat [-命令选项][vmid][间隔时间/毫秒][查询次数]
如 jstat -gc 进程id
```

Jmap 查看内存信息
堆的对象统计
```
jmap -histo 进程id > xxx.txt
```
堆内存dump
```
jmap -dump:format=b,file=temp.hprof
```
也可以设置在内存溢出时自动导出dump文件（内存过大可能会导不出来）
1. -XX:+HeapDumpOnOutOfMemoryError
2. -XX:HeapDumpPath=输出路径
```
-Xms10m -Xmx10m -XX:+PirntGCDetails -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=d:\oomdump.dump
```

Jstack 生成java虚拟机当前时刻的线程快照

##  类加载机制

#### 类的生命周期
1. 加载 将.class文件从磁盘读到内存
2. 连接
    2.1 验证
        验证字节码文件的正确性（如class文件格式、元数据、字节码、符号饮用的验证）
    2.2 准备
        给类的静态变量分配内存，并赋予默认值
    2.3 解析
        类装载器装入类所引用的其他所有类（静态链接）
3. 初始化
    为类的静态变量赋予正确的初始值，之前准备阶段赋予的是默认值，此时赋予的才是编程者为变量分配的初始值，执行静态代码块。
4. 使用
5. 卸载

#### 类加载器的种类
java中的类遵循**按需加载**。它可以使java类动态加载到jvm中运行，即可在程序运行时再加载类，提供了很灵活的动态加载方式。
**启动类加载器(Bootstrap ClassLoader)**
负责加载JRE的核心类库，如JRE目标下的rt.jar, charset.jar等
**扩展类加载器(Extension Classloader)**
负责加载JRE扩展目录ext中jar类包
**系统类加载器(Application ClassLoader)**
负责加载ClassPath路径下的类包
**用户自定义加载器(User ClassLoader)**
负责加载用户自定义路径下的类包

#### 类加载机制
**全盘负责委托机制**
当一个ClassLoader加载一个类的时候，除非显示的使用另一个ClassLoader，该类所依赖和引用的类也由这个ClassLoader载入
**双亲委派机制**
指先委托父类加载器寻找目标类，在找不到的情况下在自己的路径中查找病载入目标类
双亲委派模式的优势
- 沙箱安全机制：比如自己写的String.class类就不会被加载，这样可以防止核心库被随意篡改
- 避免类的重复加载：当父ClassLoader已经加载了该类的时候，就不需要子ClassLoader再加载一次



## GC算法和收集器
#### 如何判断对象可以被回收
堆中几乎放着所有的对象实例，对堆垃圾回收前的第一步就是要判断哪些对象已经死亡（即不能再被任何途径使用）

**引用计数法**
给对象添加一个引用计数器，每当有一个地方引用，计数器就加1。当引用失效，计数器就减1.任何时候计数器为0的对象就是不可能再被使用的。
这个方法实现简单，效率高，但是目前主流的虚拟机中没有选择这个算法来管理内存，最主要的原因是它难以解决对象之间循环引用。

**可达性分析算法**
这个算法的基本思想就是通过一系列的称为“GC Roots"的对象作为起点，从这些节点开始向下搜索，节点所走过的路径称为引用链，当一个对象到GC Roots没有任何引用链相连的话，则证明此对象是不可用的。
GC Roots根节点： 类加载器、Thread、虚拟机栈的局部变量表、static成员、常量引用、本地方法栈的变量等等

**[面试]如何判断一个常量是废弃常量？**
假如在常量池中存在字符串“abc”，如果当前没有任何string对象引用该字符串常量的话，就说明常量”abc“就是废弃常量，如果这时发生内存回收而且有必要的话，”abc“就会被系统清理出常量池。
**[面试]如何判断一个类是无用的类**
需要满足以下三个条件：
- 该类所有的实例都已经被回收，也就是java堆中不存在该类的任何实例
- 加载该类的ClassLoader已经被回收
- 该类对应的java.lang.Class对象没有在任何地方被引用，无法在任何地方通过反射反问该对象的方法。
虚拟机可以堆满足上述3个条件的无用类进行回收。这里仅仅是“可以”，而不是和对象一样不适用了就必然会回收。

#### 垃圾回收算法
**标记-清除算法**
它是最基础的收集算法，这个算法分为两个阶段，标记和清除，首先标记处所有需要回收的对象，在标记完成后统一回收被标记的对象。有两个不足：效率低，空间碎片
**复制算法**
把内存分为大小相同的两块，每次只使用其中的一块。当这一块的内存使用完后，将当中还存活的对象复制到另一块去，然后把使用的空间一次清理掉。这样就使每次的内存回收都是对内存区间的一半进行回收
**标记-整理算法**
根据老年代的特点提出的一种标记算法，标记过程和“标记-清除”算法一样，但之后不是直接对可回收对象进行回收，而是将存活的对象向一端移动，然后直接清除掉边界外的内存。
**分代收集算法**
所有商用虚拟机的垃圾收集器都采用分代收集算法。它是根据存活周期将内存分为几块，包括新生代和老年代。新生代中，每次收集都有大量对象死去，所以选择复制算法。而老年代的对象存活几率较高，而且没有额外的空间对它进行分配担保，所以选用“标记整理” 算法进行垃圾收集。

#### 垃圾收集器

**Serial收集器**
串行收集器。单线程收集器，只会使用一条线程去完成垃圾收集工作，同时暂停其他所有工作线程直到收集结束。
新生代采用复制算法，老年代采用标记-整理算法。
对于运行在Client模式下的虚拟机来说是一个不错的选择。

**ParNew收集器**
就是Serial收集器的多线程版本。
新生代采用复制算法，老年代采用标记-整理算法。
它是许多运行在Server模式下的虚拟机的首要选择，除了Serial收集器外，只有它能与CMS收集器配合工作。

**Parallel Scanvenge收集器**
类似ParNew收集器，更关注吞吐量，提供很多工具帮助用户找到合适的停顿时间或最大吞吐量（吞吐量就是cpu中用于运行用户代码的时间与cpu总消耗时间的比例）

**Serial Old收集器**
Serial收集器的老年代版本。

**Parallel Old收集器**
Parallel Scanvenge收集器的老年代版本。

**CMS(Concurrent Mark Sweep)收集器**
并行和并发：
- 并行(Parallel)：指多条垃圾收集线程并行工作，但此时用户线程仍处于等待状态
- 并发(Concurrent)：指用户线程与垃圾收集线程同时执行（但不一定是并行，可能会交替执行），用户程序在继续运行，而垃圾收集器运行在另一个CPU上。
一种以最短回收停顿时间为目标的收集器，是HotSpot虚拟机第一款真正意义上的并发收集器，实现了垃圾收集线程与用户线程（基本上）同时工作。CMS是一种“标记-清除”算法实现，整个过程分为四个步骤：
- 初始标记(initial mark)：暂停所有其他线程，并记录下直接与root相连的对象，速度很快
- 并发标记(concurrent mark)：同时开启GC和用户线程，用一个闭包结构去记录可达对象。因为用户线程可能会不断的更新引用域，所以GC线程无法保证可达性分析的实效性，所以这个算法里会跟踪记录这些发生引用更新的地方。
- 重新标记(remark)：重新标记阶段就是为了修正并发标记期间因为用户程序继续运行而导致标记产生变动的标记记录，这个阶段的停顿时间一般回避初始标记阶段的时间稍长，但远远比并发标记阶段时间短。
- 并发清除(concurrent sweep)：开启用户线程，同时GC线程开始对标记的区域做清扫。
主要优点：并发收集、低停顿。
主要缺点：对cpu资源敏感，无法处理浮动垃圾，使用的回收算法（“标记-清除”）会产生大量碎片。

**G1(Garbage-First)收集器
官方推荐，一款面向服务器的垃圾收集器，主要针对配备多个处理器及大容量内存的机器。以极高概率满足GC停顿时间要求，同时还具备高吞吐量的特性。
具备以下特点：
- 并行和并发
- 分代收集
- 可预测的停顿
大致步骤：
- 初始标记
- 并发标记
- 最终标记
- 筛选回收

## 调优
JVM调优主要就是调整下面两个指标
停顿时间(Pause GC Time)：垃圾收集器做垃圾回收中断应用执行的时间。 -XX:MaxGCPauseMillis
吞吐量(Throughput)：用户代码运行时间和总时间的占比：-XX:GCTimeRatio=n

#### GC调优步骤
1. 打印GC日志
```
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -Xloggc:./gc.log
```
Tomcat可以直接加载JAVA_OPTS变量里
2. 分析日志得到关键性指标
3. 分析GC原因，调优JVM参数

可以用GCEasy对日志可视化


## 锁

#### 多线程同步方式
1. wait/notify
2. synchronized
3. ReentrantLock
4. ReentrantReadWriteLock
5. CountdownLatch
...

#### 实现方法
1. 自旋
```java
// pseudo-code
volatile int status=0;//标识---是否有线程在同步块-----是否有线程上锁成功
void lock(){
    while(!compareAndSet(0,1)){
    }
    // locked logic
}
void unlock(){
    status=0;
}
boolean compareAndSet(int except,int newValue){
    //cas操作,修改status成功则返回true
}
```
耗费cpu资源。没有竞争到锁的线程会一直空转（占用cpu资源进行cas操作）

2. yield + 自旋
```java
// pseudo-code
volatile int status=0;
void lock(){
    while(!compareAndSet(0,1)){
     yield();
    }
    // locked logic
    unlock()
}
void unlock(){
    status=0;
}
```
当线程竞争锁失败时，会调用yield方法让出cpu。自旋+yield的方式并没有完全解决问题，当系统只有两个线程竞争锁时，yield是有效的。需要注意的是该方法只是当前让出cpu，有可能操作系统下次还是选择运行该线程

3. sleep + 自旋
```java
// pseudo-code
volatile int status=0;
void lock(){
    while(!compareAndSet(0,1)){
     sleep(10);
    }
    // locked logic
    unlock()
}
void unlock(){
    status=0;
}
```
sleep将当前线程释放cpu并阻塞，但时间不好控制

4. sleep + 自旋
```java
// pseudo-code
volatile int status=0;
Queue parkQueue;

void lock(){
    while(!compareAndSet(0,1)){
        //
        park();
    }
    // locked logic
   unlock()
}

void unlock(){
    lock_notify();
}

void park(){
    //将当前线程加入到等待队列
    parkQueue.add(currentThread);
    //将当前线程释放cpu  阻塞
    releaseCpu();
}
void lock_notify(){
    status=0;
    //得到要唤醒的线程头部线程
    Thread t=parkQueue.header();
    //唤醒等待线程
    unpark(t);
}
```

#### AQS(AbstractQueuedSynchronizer)

**技术栈**
1. 自旋
2. park/unpark
3. CAS

**两个实现类**
1. `NonfairSync`
2. `FairSync`

**应用场景**
线程执行模式有两种：交替执行和竞争执行。
java1.6前，reentrantLock遇到单线程或多线程交替执行时会直接在jdk级别上操作，但遇到竞争执行则用到队列和os的api(park&unpark)。而synchronized无论哪种都会调用os函数去解决，所以前者性能更高。
java1.6之后，synchronized遇到没单线程或多线程交替执行时会采用偏向锁，在竞争执行时

**逻辑**
公平锁：
1. 判断锁是否是自由状态(`c == 0`)
2. 如果是，则判断自己是否需要排队(`hasQueuedPredecessors()`)，
    2.1 如果需要，
    2.2 如果不需要，则CAS加锁
3. 如果不是，则判断是否已经持有锁
    3.1 如果持有锁，则把锁状态+1
    3.2 如果非持有，则入队并阻塞(`acquireQueued(addWaiter(Node.EXCLUSIVE), arg)`)

**核心代码**

*AbstractQueuedSynchronizer.java*
```java
// 主要属性
private transient volatile Node head; //队首
private transient volatile Node tail;//尾
private volatile int state;//锁状态，加锁成功则为1，重入+1 解锁则为0
private transient Thread exclusiveOwnerThread;//持有锁的线程

public class Node{
    volatile Node prev;
    volatile Node next;
    volatile Thread thread;
    volatile int waitStatus; // 默认是0, CANCELLED:1, SIGNAL:-1, CONDITION:-2, PROPAGATE:-3
}

public final void acquire(int arg) {
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}

// 入队后自选两次，若仍拿不到锁则阻塞
final boolean acquireQueued(final Node node, int arg) {
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) { // 如果当前线程是队列中第一个等待的，则尝试拿锁
                    setHead(node);
                    p.next = null; // help GC
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) && // 通过上节点的waitStatus检查是否要阻塞
                    parkAndCheckInterrupt()) // 调用LockSupport.park(this)进行阻塞
                    interrupted = true;
            }
        } catch (Throwable t) {
            cancelAcquire(node);
            throw t;
        }
    }
// 判断是否要排队
public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t && // 队列是否初始化
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }

/**
 * Creates and enqueues node for current thread and given mode.
 *
 * @param mode Node.EXCLUSIVE for exclusive, Node.SHARED for shared
 * @return the new node
 */
private Node addWaiter(Node mode) {
    Node node = new Node(mode);

    for (;;) {
        Node oldTail = tail;
        if (oldTail != null) {
            U.putObject(node, Node.PREV, oldTail);
            if (compareAndSetTail(oldTail, node)) {
                oldTail.next = node;
                return node;
            }
        } else {
            initializeSyncQueue();
        }
    }
}

/**
 * Convenience method to park and then check if interrupted.
 *
 * @return {@code true} if interrupted
 */
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}

```

*ReentrantLock.java*
```java
public class ReentrantLock implements Lock, java.io.Serializable {

    private final Sync sync;

    /**
     * 抽象锁类
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {

        /**
         * Performs non-fair tryLock.  tryAcquire is implemented in
         * subclasses, but both need nonfair try for trylock method.
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) { // 锁是否是自由状态
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) { // 是否已经持有锁
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    /**
     * 非公平锁类
     */
    static final class NonfairSync extends Sync {

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * 公平锁类
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) { // 锁是否是自由状态
                if (!hasQueuedPredecessors() && // 是否需要排队
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current); // 设置exclusiveOwnerThread为当前线程
                    return true;
                }
            }
            // 重入
            else if (current == getExclusiveOwnerThread()) { // 是否已经持有锁
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    // 构造方法

    /**
     * 默认实例化为非公平锁
     */
    public ReentrantLock() {
        sync = new NonfairSync();
    }

    /**
     * 传入true将实例化为公平锁
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    // api

    public void lock() {
        sync.lock();
    }

    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }
}
```
#### JOL(Java Object Layout)

**查看对象信息**

```xml
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.8</version>
</dependency>
```

```java
System.out.println(ClassLayout.parseInstance(sampleObject).toPrintable());
```

**java对象组成**
1. 对象头 （64位os上占12个字节，合96bit)

头对象结构 | 大小 | 说明
--- | --- | ---
Mark word | 64bit | 对象头第一部分，存储对象的hashCode(56bit)、分代年龄(4bit)、是否为偏向锁(1bit)、锁标志位(2bit)及其他信息*
Klass pointer | 32bit | 对象头第二部分，类型指针，指向对象的类元数据，JVM通过这个指针确定该对象是哪个类的实例
Length | nan | 仅数组对象有，表示数组长度

* 注：java的对象头在对象的不同状态下会有不同的表现形式：
```
|----------------------------------------------------------------------------------------------------------------------------------------------|
|                                           Object Header (128 bits)                                        |     Object Status    | Lock Flag |
|-----------------------------------------------------------------------------------------------------------|----------------------|-----------|
|                     Mark Word (64 bits)                                        |   Klass Word (64 bits)   |                      |           |
|--------------------------------------------------------------------------------|--------------------------|----------------------|-----------|
|  unused:25 | identity_hashcode:31 | unused:1 | age:4 | biased_lock:1 | lock:2  |  OOP to metadata object  |        Normal        |   0 01    |  无锁
|--------------------------------------------------------------------------------|--------------------------|----------------------|-----------|
|  thread*:54 |       epoch:2       | unused:1 | age:4 | biased_lock:1 | lock:2  |  OOP to metadata object  |        Biased        |   1 01    |  偏向锁（单线程重入）
|--------------------------------------------------------------------------------|--------------------------|----------------------|-----------|
|                        ptr_to_lock_record:62                         | lock:2  |                          |  Lightweight Locked  |   0 00    |  轻量锁（多线程交替执行）
|--------------------------------------------------------------------------------|--------------------------|----------------------|-----------|
|                    ptr_to_heavyweight_monitor:62                     | lock:2  |                          |  Heavyweight Locked  |   0 10    |  重量锁（多线程竞争执行）
|--------------------------------------------------------------------------------|--------------------------|----------------------|-----------|
|                                                                      | lock:2  |                          |    Marked for GC     |   0 11    |  GC
|----------------------------------------------------------------------------------------------------------------------------------------------|
```

2. 实例数据

3. 填充数据（对齐字节，Java对象字节长度必须是8的倍数）

## 代理

#### 静态代理
静态代理在使用时需要定义接口或者父类。

- 继承
代理对象继承目标对象，重写目标对象的方法。
目标对象:
```java
public class UserDaoImpl {
    public void login(){
        System.out.println("login success...");
    }
}
```
代理对象：
```java
public class UserLogProxy extends UserDaoImpl{
    @Override
    public void login(){
        System.out.println("---log---");
        super.login();
    }
}
```
缺点：
1.java是单继承，难以实现扩展
2.如果每个类都需要代理，代理类会爆炸

2.聚合
目标对象和代理对象实现同一个接口，并且代理对象中包含抽象对象。
抽象对象：
```java
public interface UserDao {
    void login()
}
```
目标对象：
```java
public class UserDaoImpl implements UserDao{
    @Override
    public void login(){
        System.out.println("login success...");
    }
}
```
代理对象：
```java
public class UserLogImpl implements UserDao{
    private UserDao target;
    public UserLogImpl(UserDao target){
        this.target = target;
    }
    @Override
    public void login(){
        System.out.println("---login---");
        target.login();
    }
}
```
缺点：类爆炸

#### 动态代理
JDK动态代理
1. 实现InvocationHandler接口来定义自己的InvocationHandler
```java
public class MyHandler implements InvocationHandler {
    private Object object;
    public MyHandler(Object object){
        this.object = object;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 前置处理
        System.out.println("before log");
        Object invoke = method.invoke(object, args);
        // 后置处理
        System.out.println("after log");
        return invoke;
    }
}
```
2.



## IO

#### BIO
*BIOServer.java*
```java
public class BIOServer {
    static byte[] bytes = new byte[1024]
    public static void main(String[] args){
        try{
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(8080));

            while(true){
                // blocking
                Socket socket = serverSocket.accept();
                // blocking
                int readBytes = socket.getInputStream().read(bytes);
                String content = new String(bytes);
                System.out.println(content);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
```
*BIOClient.java*
```java
public class Client{
    public static void main(String[] args){
        try{
            Socket socket = new Socket("127.0.0.1", 8081);
            socket.connect(new InetSocketAddress(8080));
            socket.getOutputStream().write("xxx".getBytes());
        }c atch (IOException e){
            e.printStackTrace();
        }
    }
}
```
#### 缺点
- 在不考虑多线程情况下，bio无法处理并发。
- 如果开很多线程，碰到连接上但不活跃的时候，依然会阻塞在read阶段，浪费cpu资源

#### 改进
单线程处理并发(多路复用：多个网络连接复用同一个线程)
```java
public class BIOServer {

    static byte[] bytes = new byte[1024];

    static List<SocketChannel> socketChannelList = new ArrayList<SocketChannel>()；

    public static void main(String[] args){
        try{
            ServerSocketChannel serverSocketChannel = new ServerSocketChannel();
            serverSocketChannel.bind(new InetSocketAddress(8080));
            // set serverSocket non-blocking
            serverSocketChannel.configureBlocking(false);
            while(true){

                // non-blocking operation
                SocketChannel socketChannel = serverSocket.accept();
                if(socketChannel == null){
                    // continue
                } else {
                    // set socket non-blocking
                    socket.setConfig(false);
                    list.add(socket);
                }

                for(SocketChannel channel: socketChannelList){
                    // non-blocking operation
                    ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                    int readBytes = channel.read(byteBuffer);

                    if(readByes != 0){
                        byteBuffer.flip(); //写->读
                        System.out.println(new String(byteBuffer.array()));
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
```
缺点：依然解决不了不活跃线程浪费cpu的问题，对每个线程是否接收数据作轮询太耗资源

#### 再改进
将上面for循环读数据的操作交给操作系统处理，操作系统会调用相关函数。比如linux的epoll和windows的select。

*redis底层选用IO通信模型也是用到了epoll，epoll当有通信来的时候可以直接定位到那个socket，而不需要循环。所以比select效率高，能支持高并发高可用

#### NIO
> Non-blocking io uses a single thread or only a small number of multi-threads. Each connection shares a single thread. Thread resources can be released to handle other requests while waiting (without events). The main thread allocates resources to handle related events by notifying (waking up) the main thread through event-driven model when events such as accept/read/write occur. java.nio.channels.Selector is the observer of events in this model. It can register multiple events of SocketChannel on a Selector. When no event occurs, the Selector is blocked and wakes up the Selector when events such as accept/read/write occur in SocketChannel.(*[Analysis of NIO selector principle](https://programmer.ink/think/analysis-of-nio-selector-principle.html)*)

![NIO Diagram](https://programmer.ink/images/think/108539a1a691325e31b8f18a04e2a52d.jpg)

```java
selector = Selector.open();

ServerSocketChannel ssc = ServerSocketChannel.open();
ssc.configureBlocking(false);
ssc.socket().bind(new InetSocketAddress(port));

ssc.register(selector, SelectionKey.OP_ACCEPT);

while (true) {

    // select() block, waiting for an event to wake up
    int selected = selector.select();

    if (selected > 0) {
        Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
        while (selectedKeys.hasNext()) {
            SelectionKey key = selectedKeys.next();
            if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                // Handling accept events
            } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                // Handling read events
            } else if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
                // Handling write events
            }
            selectedKeys.remove();
        }
    }
}
```


## 面试
1. mysql和tomcat是如何打破双亲委派机制的？tomcat自己的类加载机制？
2. 有几种引用？强软弱虚


### 参考
周志明 《深入理解java虚拟机》
