# Spring

## 循环依赖(CircularReference)
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
AnnotationAwareAspectJAutoProxyCreator | 处理AOP
ApplicationContextAwareProcessor | 当应用程序定义的bean实现ApplicationContextAware接口时注入ApplicationContext对象
InitDestroyAnnotationBeanPostProcessor | 处理自定义的生命周期初始化和销毁方法
BeanVlidationPostProcessor | ...



#### spring容器
spring中各种组件的集合叫做spring容器，包括beanFactory, beanDefinition, singletonObjects单例池, singletonFactories作循环依赖用的二级缓存

## AOP代理

#### 手段
1. spring AOP
2. AspectJ
两者都采用AspectJ语法

#### 使用
1. **启用对AspectJ语法的支持**
java:
```java
@EnableAspectJAutoProxy
```
2. **声明切点**
```java
@Component
@Aspect
public class SampleAspect {
    @Pointcut("execution(* com.xyz.service..*.*(..))")
    private void samplePointcut() {};
};
```
3. **声明通知**
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
#### 基于xml搭建
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

#### 基于java搭建
```java
public class MyWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletCxt) {

        // Load Spring web application configuration
        AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
        ac.register(AppConfig.class);
        ac.refresh();

        // Create and register the DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(ac);
        ServletRegistration.Dynamic registration = servletCxt.addServlet("app", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/app/*");
    }
}
```
