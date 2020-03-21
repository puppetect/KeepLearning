# Spring

## IoC (Inversion of Control)
#### 实例化过程
```
>> 扫描
    -> AbstractApplicationContext.invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory)
>> BeanDefinition >> (put map) >> beanFactory.beanDefinitionMap >> (二次扩展)
>> BeanFactoryPostProcessor >> (update) >> beanFactory.beanDefinitionMap
>> 初始化（验证并实例化非懒加载单例对象）
    -> AbstractApplicationContext.finishBeanFactoryInitialization(ConfigurableListableBeanFactorybeanFactory) ;
    -> DefaultListableBeanFactory.preInstantiateSingletons();
    -> DefaultListableBeanFactory.getBean(beanName);
    -> AbstractBeanFactory.doGetBean(name, null, null, false);
    -> DefaultSingletonBeanRegistry.getSingleton(beanName);
    如果上述返回null，用回调函数调用下面函数
    -> AbstractAutowireCapableBeanFactory.createBean(beanName, mbd, args);
    -> AbstractAutowireCapableBeanFactory.resolveBeforeInstantiation(beanName, mbd);(第一次调用后置处理器)
    -> AbstractAutowireCapableBeanFactory.applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
>> bean单例缓存池 singletonObjects
```

#### 创建bean时的三个缓存
cache | type | name | remarks
--- | --- | --- | ---
一级缓存（单例池） | singletonObjects | Map<String, Object> | Cache of registered singletons, containing the bean names in registration order.
二级缓存（工厂） | singletonFactories | Map<String, ObjectFactory<?>> | Cache of singleton factories: bean name to ObjectFactory.
三级缓存 | Map<String, Object> | earlySingletonObjects | Cache of early singleton objects: bean name to bean instance.
*辅助集合 | Set<String> | singletonsCurrentlyInCreation | Names of beans that are currently in creation.

#### bean的生命周期
*(AbstractAutowireCapableBeanFactory.java)*
1. **创建对象**
```java
createBeanInstance(beanName, mbd, args);
```
2. **填充属性（自动注入）**
```java
populateBean(beanName, mbd, instanceWrapper);
```
3. **执行生命周期回调方法**
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

#### bean的实例化
> 以下阶段，spring基本没有提供扩展点给程序员
1. 父类的构造方法`new DefaultListableBeanFactory()`对bean工厂进行初始化。为何要初始化？因为`beanFactory`中有很多重要的属性，比如单例池
2. `AnnotationConfigApplicationContext`的**无参构造方法**，无参构造方法里面，往`beanDefinitionMap`当中注册很多`beanDefinition`，其中最重要的一个，就是`ConfigurableClassPostProcessor`
3. 实例化一些功能性对象`AnnotatedBeanDefinitionReader`、`ClassPathBeanDefinitionScanner`
4. `register(annotatedClasses)`注册配置类到`beanDefinitionMap`中，方便后面实例化这个配置类，配置类为何要实例化？首先配置类也是一个bean，自然要实例化，其次配置里面有很多`@Bean`注解到方法，需要进行CGLIB代理，所以一定要实例化。而创世纪的几个类，则是在无参构造方法里直接注册的。
5. refresh()
6. 第一个`invokeBeanFactoryPostProcessors`，执行用户和spring提供的`BeanDefinitionRegistryPostProcessor`和`BeanFactoryPostProcessor`的实现类。
7. `BeanDefinitionRegistryPostProcessor-ConfigurationClassPostProcessor-postProcessBeanDefinitionRegistry`，类的扫描（扫描包含普通扫描，也包含@Bean的扫描）和类的解析成beanDefinition对象，比如Import的解析也在这个方法里面。Import有分为三种类型，import一个普通类，`ImportSelector`，`ImportBeanDefinitionRegistrar`
8. `BeanFactoryPostProcessor-ConfigurationClassPostProcessor-postProcessBeanFactory`，判断我们的配置类是不是全配置类full，如果是full需要给配置类加上CGLIB代理
9. 第二个`registerBeanPostProcessors`，注册spring当中的后置处理器（包括程序员提供的），`beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount))`这些后置处理器，分为程序员提供的和spring内部的，如果是程序员提供的，那么需要加上@Component注解，spring会扫描到单例池中，如果是spring内部的，则不在单例池中，仅仅是维护在beanFactory的一个集合变量当中。不在单例池中就不能通过getBean得到。
10. 第三个`finishBeanFactoryInitialization`，finishBeanFactoryInitialization初始化单例bean。
> 以下阶段，spring的bean的实例化过程，严格意义上讲上面的也算bean的实例化过程
11. getSingleton(String beanName, boolean allowEarlyReference)获取一个bean，首先从单例池中获取，若获取不到，再从singletonFactories当中获取。singletonFactories是spring提前暴露的对象，如果得到了判断是不是FactoryBean，否则直接返回。
12. getSingleton(String, ObjectFactory<?>)也是首先从单例池中拿，拿不到则调用singletonFactory.getObject()创建对象。
13. InstantiationAwareBeanPostProcessor.postBeforeInstantiation 如果这个方法返回了对象，spring只会执行BeanPostProcessor的postProcessAfterInitialization()
14. `doCreateBean->createBeanInstance()`第二次调用后置处理器
15. `SmartInstantiationAwareBeanPostProcessor-determinCandidateConstructors`推断构造方法
16. 对象被new出来了（仅仅是一个寡对象）
17. `applyMergedBeanDefinitionPostprocessors`
18. `MergedBeanDefinitionPostProcessor-postProcessorMergedBeanDefinition`找出并缓存对象的注解的信息，主要是自动注入
19. 第四次调用后置处理器，判断是否需要AOP，`addSingletonFactory`--把ObjectFactory对象（包含了访问该对象的API）缓存起来--singletonFactories、singletonFactories当中提供了API访问刚刚创建的对象，而这个API就是一个后置处理的方法。
> 以下阶段，spring的bean初始化过程，初始化过程包含在实例化的过程中
20. `populateBean`填充属性、自动注入，这个方法是spring当中极为重要的方法
21. `InstantiationAwareBeanPostProcessor-postProcessAfterInstantiation`判断对象是否需要填充属性
22. `InstantiationAwareBeanPostProcessor-postProcessPropertyValues`完成装配，即完成属性的注入，也就是大家常常说的自动注入
23. `BeanPostProcessor-postProcessBeforeInitialization`
24. `invokeInitMethods`--执行spring的生命周期方法--init
25. `BeanPostProcessor-postProcessAfterInitialization`这个方法较简单，经典的应用场景就是AOP的代理

#### 生命周期初始化回调方法
*（按照执行顺序）*
1. @PostConstruct注释的方法
```java
@PostConstruct
public void init() {}

@PreDestroy
public void destroy() {}
```
2. InitializingBean接口应用类的afterPropertiesSet()方法，及DisposableBean接口应用类的destroy()方法
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

## AOP (Aspect Oriented Programming)

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
Advice | 通知 | 切入连接点的位置和时机
Aspect | 切面 | 切点、连接点和通知模块化后所在的类称为切面
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

4. createProxy有2种创建方法，JDK动态代理或CGLIB

   a. 如果设置了proxyTargetClass=true，一定是CGLIB代理
   b. 如果proxyTargetClass=false，目标对象实现了接口，走JDK动态代理
   c. 如果没有实现接口，走CGLIB代理
   JDK代理的实现方式是基于接口实现，代理类继承Proxy，实现接口。而CGLIB继承被代理的类来实现。

## Spring MVC

#### 原生Servlet3.0
*IndexServlet.java*
```java
@WebServlet("/index")
public class IndexServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        super.doGet(req, resp);
    }
}
```
缺点：
没有模块化，每个Servlet只能处理一种业务逻辑

#### 自制dispatcherServlet
*DispatcherServlet.java*
```java
@WebServlet(value="/*", loadOnStartup=1) // loadOnStartup设置启动顺序，数值越小启动越早
public class DispatcherServlet extends HttpServlet {
    Map<String, Method> map = new HashMap<String, Method>();
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        // 1.获取path
        String path = req.getRequestURI();
        path = path.substring(path.lastIndexOf;
        // 2.把path和对应的方法放到map中
        Method method = map.get(path);
        Class targetClazz = method.getDeclaringClass();
        try{
            Object targetObject = targetClazz.getInstance();
            Class[] paramClazz = method.getParameterTypes();
            if(paramClazz.length == 0){
                method.invoke(targetObject);
            } else if(paramClazz.length == 1){
                Class tempCls = paramClazz[0];
                if(tempCls.getSimpleName().equals("HttpServletRequest")){
                    method.invoke(targetObject, req);
                } else if(tempCls.getSimpleName().equals("HttpServletResponse")){
                    method.invoke(targetObject, resp);
                }
            } else if (paramClazz.length == 2) {
                Class tempCls = paramClazz[0];
                if(tempCls.getSimpleName().equals("HttpServletRequest")){
                    method.invoke(targetObject, req, resp);
                } else if(tempCls.getSimpleName().equals("HttpServletResponse")){
                    method.invoke(targetObject, resp, req);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws ServletException {
        String rootPath = this.getClass().getResource("/").getPath(); // 得到类所属包所在的根路径
        rootPath += "\\com\\samplePackage\\";
        File file = new File(rootPath);
        File[] files = file.listFiles();
        for(File tempFile: files){
            try{
                Class clazz = Class.forName("com.samplePackage"+tempFile.getName().replaceAll(".class",""));
                Method[] methods = clazz.getDeclaredMethods();
                for(Method method: methods){
                    if(method.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping rm = method.getDeclaredAnnotation(RequestMapping.class);
                        map.put(rm.value(), method);
                    };
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
```
*RequestMapping.java*
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value();
}
```
注：要让jdk识别注解，需要定义
1. @Target: 注解出现的位置
2. @Retention: 注解的生命周期（默认只在java源码当中有，不会被编译到class中，相当于注释的效果）

实例化对象有几种方法：
1. New
2. 反射
3. 克隆
4. 序列化

#### springmvc搭建
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

#### 内嵌servlet容器(tomcat)
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

*SpringApplication.java*
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
*Test.java*
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

#### 配置类
将`@Configuration`注解的类进行代理，增强或修改类中的方法，并保证类中被`@Bean`标注的对象是单例对象




## 面试

1. spring中factoryBean和beanFactory的区别？
    beanFactory是spring中的工厂，它可以产生和获取bean
    factoryBean是一个特殊的bean，实现了FactoryBean接口，重写了三个方法：getObject, getObjectType, isSingleton。他本身是个bean，同时getObject方法返回的对象也是bean

2. spring源码中应用了哪些设计模式？
    策略(beanPostProcessor)、工厂(beanFactory)、
