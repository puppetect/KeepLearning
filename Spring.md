# Spring

## 循环依赖(Circular Reference)
#### 实例化过程
```
x.java >> (classLoader) >> x.class
>> 扫描 AbstractApplicationContext.invokeBeanFactoryPostProcessors(beanFactory)
>> BeanDefinition >> (put map) >> beanFactory.beanDefinitionMap >> (二次扩展)
>> BeanFactoryPostProcessor >> (update) >> beanFactory.beanDefinitionMap
>> 验证并实例化非懒加载单例对象 beanFactory.preInstantiateSingletons()
>> bean单例缓存池 singletonObjects
```
#### 创建bean时的三个缓存
cache | type | name | remarks
--- | --- | --- | ---
一级缓存（单例池） | singletonObjects | Map<String, Object> | Cache of registered singletons, containing the bean names in registration order.
二级缓存（工厂） | singletonFactories | Map<String, ObjectFactory<?>> | Cache of singleton factories: bean name to ObjectFactory.
三级缓存 | Map<String, Object> | earlySingletonObjects | Cache of early singleton objects: bean name to bean instance.

#### bean的生命周期
*(AbstractAutowireCapableBeanFactory.java)*
1. **创建**
```java
createBeanInstance(beanName, mbd, args);
```
2. **填充属性（自动注入）**
```java
populateBean(beanName, mbd, instanceWrapper);
```
3. **初始化**
```java
protected Object initializeBean(beanName, exposedObject, mbd){
    // 执行@PostConstruct方法
    applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
    // 执行其他接口和xml中的init方法
    invokeInitMethods(beanName, wrappedBean, mbd);
};
```
4. **AOP代理**
```java
applyBeanPostProcessorAfterInitialization(wrappedBean, beanName);
```
*注：该过程也可在填充属性的工厂方法中被提前调用，这也是二级缓存使用工厂的原因*


#### 生命周期初始化方法
*（按照执行顺序）*
1. @PostConstruct注释的方法
2. InitializingBean接口应用类的afterPropertiesSet()方法
3. 在bean的配置参数中用init-method指定的方法



#### BeanPostProcessor
BeanPostProcessor是spring框架提供的一个扩展类点（不止一个），通过实现BeanPostProcessor接口，即可介入bean实例化的过程，从而减少beanFactory的负担。比如AOP就是通过BeanPostProcessor和IoC容器建立了联系
BeanPostProcessor | Usage
--- | ---
CommonAnnotationBeanPostProcessor | 处理@Resource
AutowiredAnnotationBeanPostProcessor | 处理@Autowired
RequiredAnnotationBeanProcessor | 处理@Required
ConfigurationClassPostProcessor | 处理@Configuration
AnnotationAwareAspectJAutoProxyCreator | 处理AOP
ApplicationContextAwareProcessor | 当应用程序定义的bean实现ApplicationContextAware接口时注入ApplicationContext对象
InitDestroyAnnotationBeanPostProcessor | 处理自定义的生命周期初始化和销毁方法




#### spring容器
spring中各种组件的集合叫做spring容器，包括beanFactory, beanDefinition, singletonObjects单例池, singletonFactories作循环依赖用的二级缓存

## AOP代理

#### 手段
1. spring AOP
2. AspectJ
两者都采用AspectJ语法

#### 使用
1. 启用对AspectJ语法的支持
```java
@EnableAspectJAutoProxy
```
2. 声明切点
```java
@Component
@Aspect
public class SampleAspect {
    @Pointcut("execution(* com.xyz.service..*.*(..))")
    private void samplePointcut() {};
};
```
3. 声明通知
```java
    @Before("samplePointcut()")  //切入连接点的时机
    public void sampleAdvice() {} //切入连接点的内容
```

#### 术语
concept | chinese | meaning
--- | --- | ---
Join point | 连接点 | 方法的执行
Pointcut | 切点 | 连接点的集合
Advice | 通知 | 切入连接点的时机和切入点的业务内容
Aspect | 切面 | 切点、连接点和通知所在的类称为切面
Target object | 目标对象 | 原生对象
Proxy object | 代理对象 | 经过AOP代理的对象

#### spring AOP的实现

1. AnnotationAwareAspectJAutoProxyCreator是AOP核心处理类，它实现了BeanProcessor，其中postProcessAfterInitialization是核心方法。

2. 核心实现分为2步：

   getAdvicesAndAdvisorsForBean获取当前bean匹配的增强器；
   createProxy为当前bean创建代理

3. getAdvicesAndAdvisorsForBean核心逻辑如下：

   a. 找所有增强器，也就是所有@Aspect注解的Bean
   b. 找匹配的增强器，也就是根据@Before，@After等注解上的表达式，与当前bean进行匹配，暴露匹配上的。
   c. 对匹配的增强器进行扩展和排序，就是按照@Order或者PriorityOrdered的getOrder的数据值进行排序，越小的越靠前。

4. createProxy有2种创建方法，JDK代理或CGLIB

   a. 如果设置了proxyTargetClass=true，一定是CGLIB代理
   b. 如果proxyTargetClass=false，目标对象实现了接口，走JDK代理
   c. 如果没有实现接口，走CGLIB代理

## Spring MVC

#### 搭建
1. 导入依赖
pom.xml
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.1.6.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>5.1.6.RELEASE</version>
</dependency>
```
2.1 基于xml
web.xml
```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
</context-param>
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>

<servlet>
    <servlet-name>dispatcherServlet</servlet-name>
    <servlet-class>DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>
</servlet>
```
applicationContext.xml
```xml
<context:component-scan base-package="com.xxxx.xxService"/>
```
springmvc.xml
```xml
<context:component-scan base-package="com.xxxx.xxController"/>

<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    <property name="prefix" value="/view"></property>
    <property name="suffix" value=".jsp"></property>
</bean>
```

2.2 基于java
MyWebApplicationInitializer.java
```java
public class MyWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletCxt) {

        // 利用spring javaconfig技术来完成对spring环境的初始化
        AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
        // 将配置类放到beanDefinitionMap中，再用它结合spring javaconfig技术来完成对bean和controller的扫描
        ac.register(AppConfig.class);
        ac.refresh();

        // 利用servlet3.1提供的spi来完成servlet的注册
        DispatcherServlet servlet = new DispatcherServlet(ac);
        ServletRegistration.Dynamic registration = servletCxt.addServlet("app", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/app/*");
    }
}
```
AppConfig.java
```java
@Configuration
@ComponentScan("com.xxxx")
public class AppConfig {}
```

#### 内嵌web容器(tomcat)
1. 导入依赖
```xml
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-core</artifactId>
    <version>9.0.14</version>
</dependency>
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
    <version>8.5.45</version>
</dependency>
```
2. 启动tomcat

SpringApplication.java
```java
public class SpringApplication {
    public static void run() {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.addWebapp("/", "d:\\app\\");
        try{
            tomcat.start();
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }
}
```
Test.java
```java
public class Test {
    public static void main(String[] args){
        SpringApplication.run();
    }
}
```

#### 启动机制
1. servlet3.1的SPI机制  对于满足servlet的web容器，必须检查`META-INF/services/javax.servlet.ServletContainerInitializer`文件，如果文件中提供了一个`ServletContainerInitializer`接口的实现类(`org.springframework.web.SpringServletContainerInitializer`)，那么容器在启动时将无条件地调用这个实现类的onStartup方法
2. 在容器回调onStartup方法时，会传入加到`@HandlesTypes()`注解中的接口`WebApplicationInitializer`的所有实现类

SpringServletContainerInitializer.java
```java

@HandlesTypes({WebApplicationInitializer.class})
public class SpringServletContainerInitializer implements ServletContainerInitializer {
    public void onStartup(@Nullalbe Set<Class<?>> webAppInitializerClasses, ServletContext servletContext) throws ServletException {
        // 省略
        }
    }
}
```

#### spring javaconfig
spring的java注解技术

## Configuration

#### 作用
将`@Configuration`注解的类进行代理，增强或修改类中的方法，并保证类中被`@Bean`标注的对象是单例对象

## 并发

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
sleep将当期线程释放cpu并阻塞，但时间不好控制

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
    //将当期线程加入到等待队列
    parkQueue.add(currentThread);
    //将当期线程释放cpu  阻塞
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

**主要属性**
```java
private transient volatile Node head; //队首
private transient volatile Node tail;//尾
private volatile int state;//锁状态，加锁成功则为1，重入+1 解锁则为0
private transient Thread exclusiveOwnerThread;//持有锁的线程
// Node类
public class Node{
    volatile Node prev;
    volatile Node next;
    volatile Thread thread;
}
```

**主要技术栈**
1. 自旋
2. park/unpark
3. CAS

**两个实现类**
1. `NonfairSync`
2. `FairSync`

**应用场景**
线程执行模式有两种：交替执行和竞争执行。
java1.6前，reentrantLock遇到单个线程或多线程交替执行只会在jdk级别解决同步问题，只有竞争执行才会用到队列和os的api(park&unpark)。而synchronized无论哪种都会调用os函数去解决，所以前者性能更高
