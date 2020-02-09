# MicroService

## Dubbo

#### 简介

Apache Dubbo是一款高性能Java RPC框架

**RPC (Remote Procedure Call)**

远程过程调用是一个**计算机通信协议**。该协议允许运行于一台计算机的程序调用另一台计算机的子程序，而程序员就像调用本地程序一样，无需额外地为这个交互作用编程。如果涉及的软件采用面向对象编程，那么远程过程调用亦可称作远程调用或远程方法调用，例：Java RMI。

**service Mesh**
??

#### 工程

**接口 interface**
1.接口类
**消费者 consumer**
1. 启动类
2. 配置文件
```xml
<dubbo:application nae='consumerName'/>
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

## Spring Cloud
