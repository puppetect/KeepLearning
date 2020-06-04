# Spring Cloud

## Spring Cloud Netflix

### 传统架构

**传统架构缺点**
1.单体架构，大多只能部署在一台或几台电脑上，高并发比较困难
2. 耦合度比较大，开发维护困难
3. 改变一个单元需要重新打包整个项目，更新麻烦

**微服务架构**
1.每个模块都是独立进程
2. 如果某个模块出问题不会影响其他模块运行

**4大原则**
1.高内聚低耦合
2.前后端分离
3.无状态访问
4.restful风格

#### 服务注册与发现组件
- Eureka 追求高可用性(AP)，默认数据存在内存
- Zookeeper 追求数据强一致(CP)，默认数据存在磁盘

#### 通信协议
Spring CLoud 应用层通信协议： http
Dubbo 应用层通信协议： rpc

**RPC (Remote Procedure Call)**

远程过程调用是一个**计算机通信协议**。该协议允许运行于一台计算机的程序调用另一台计算机的子程序，而程序员就像调用本地程序一样，无需额外地为这个交互作用编程。如果涉及的软件采用面向对象编程，那么远程过程调用亦可称作远程调用或远程方法调用，例：Java RMI。

#### Eureka搭建
**Eureka Server**
*pom.xml*
```xml
<parent>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
	<version>2.2.2.RELEASE</version>
</parent>
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-dependencies</artifactId>
			<version>Hoxton.SR1</version> <!-- 需要用最新的 -->
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
<dependencies>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-config</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
	</dependency>
</dependencies>
```
*application.yml*
```yml
server:
	port:8761
eureka:
	server:
		enable-self-preservation: false
		eviction-internal-timer-in-ms: 30000
	instance:
		hostname:localhost
	client:
		registerWithEureka: false
		fetchRegistry: false
		serviceUrl:
			defaultZone: http://${uereka.instance.hostname}:${server.port}/eureka
```
*EurekaApplication*
```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaApplication {
	public static void main(String[] args){
		SpringApplication.run(EurekaApplication.class, args);
	}
}
```

**Eureka Client**

**Service Publisher**
*pom.xml*
```xml
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-dependencies</artifactId>
			<version>Hoxton.SR1</version> <!-- 需要用最新的 -->
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
<dependencies>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
	</dependency>
</dependencies>
```
*application.yml*
```yml
server:
	port:9001/9002 # 假设注册了两个相同的服务，只是端口不同
eureka:
	instance:
		hostname:localhost
	client:
		serviceUrl:
			defaultZone: http://${eureka.instance.hostname}:8761/eureka
spring:
	application:
		name: sampleService
```
*SampleApplication.java*
```java
@SpringBootApplication
@EnableEurekaClient
public class SampleApplication {
	public static void main(String[] args){
		SpringApplication.run(SampleApplication.class, args);
	}
}
```

**Service Consumer**
*AppConfig.java*
```java
@Configuration
public class AppConfig {
	@Bean
	@LoadBalanced // ribbon实现客户端负载均衡
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
```

*comsumerController.java*
```java
@Controller
public class consumerController {
	@Autowired
	RestTemplate restTemplate;

	@RestMapping("consume.do")
	@ResponseBody
	public String consume(){
		return restTemplate.getForEntity("http://sampleService/get", String.class).getBody();
	}
}

```


#### Eureka启动过程
1. 每个微服务启动时发送一个携带注册信息的http请求到注册中心，eureka用ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>维护各个微服务的注册信息
2. 定时去注册中心获取注册信息


#### Eureka注册中心底层原理（责任链模式）
```
ApplicationResource.addInstance(info,...)
-> InstanceRegistry.register(info,...)
--> InstanceRegistry.handleRegistration(info,...) //InstanceRegistry //负责发布事件
--> PeerAwareInstanceRegistry.replicateToPeers(Action.Register, info,...) //集群信息同步
--> AbstractInstanceRegistry.register(info,...)
```

**spring事件驱动器**

*InstanceRegistry.java* 注册类
```java
private void handleRegistration(info, ...){
	//...
	publishEvent(new EurekaInstanceReisteredEvent(this, info, ...));
}
```
*EurekaRegisterListener.java* 自定义监听类
```java
@Componnet
public class EurekaRegisterListener {

	@EventListener
	public void register(EurekaInstanceReisteredEvent E){
		System.out.print("接收到注册请求。为服务名：" + e.getInstanceInfo().getAppName9);
	}
}
```

#### 面试
Q: 当注册的时候发生名字冲突的时候怎么办？
A: eureka会拿已经存在和当前要注册的注册信息的最后活跃时间来对比，如果没有更新，则用现成的。

#### 客户端负载均衡
- Ribbon / Feign / Spring CLoud LoadBalancer
- 在客户端（即调用方）完成负载均衡，而不是类似于nginx在服务器端（被调用方）完成
- 运用了eureka**服务注册与发现组件**功能，ribbon/feign能够从eureka发现有哪些组件，继而决定调用哪一个，并完成调用

## Dubbo

#### 简介

Apache Dubbo是一款高性能Java RPC框架



#### 工程

**接口 interface**
1.接口类
**消费者 consumer**
1. 启动类
2. 配置文件
```xml
<dubbo:application name='consumerName'/>
<dubbo:registry address="zookeeper://127.0.0.1:2181"/>
<!-- generate proxy for the remote service, then can be used in the same way as local interface -->
<dubbo:reference id="demoService" check="false" interface="com.xxx.DemoService"/>
```

**生产者 provider**
1. 启动类
2. 接口实现类
3. 配置文件
```xml
<!-- provider's application name, used for tracing dependency relationship -->
<dubbo:application name="providerName"/>
<dubbo:registry address="zookeeper://127.0.0.1:2181"/>
<!-- use dubbo protocol to export service on port 20880 -->
<dubbo:protocol name="dubbo"/>
<!-- service implementation, as same as regular local bean -->
<bean id="demoService" class="com.xxx.DemoServiceImpl"/>
<!-- declare the service interface to be exported -->
<dubbo:service interface="com.xxx.DemoService" ref="demoService"/>
```

#### 实现
1. Provider模块：提供API、实现API、暴露（启动tomcat, nettyServer）、服务本地注册、服务注册中心注册
2. Consumer模块：拿接口名从注册中心获取服务地址、调用服务
3. Registry模块：保存服务配置信息（服务名：List<URL>)
4. RpcProtocol模块：基于Tomcat的HttpProtocol、基于Netty的DubboProtocol
5. Framework模块：框架实现

## Spring Cloud Function

#### What's SCF
- Promotes implementation of business logic as functions
	- Supplier<O>
	- Function<I, O>
	- Consumer<I>
- Uniformed programming model
- Transparent type conversion
- Function Composition
- POJO functions (if it looks like a function it must be a function)
- Reactive support
- Arity (functions with multiple inputs/outputs)
- Deployment of packaged functions (JARs or exploded archives)
	- Boot configuration
	- Simple Spring configuration
	- Simple non-Spring packages
- Integration with serverles platforms
	- AWS
	- Azure

#### Core abstractions
- Function Catalog
	- Acts as a function registry
	- Wraps functions to add additioal features
- Function Registration
	- Encapsulates required information about functions
		- Input/Output types
	- Used for manual function registration

## Spring Cloud Gateway
