# Tomcat

## 部署

四种方式
1. **deployDescriptors** 文件描述符
Deploy XML descriptors from configBase
e.g * Catalina/localhost/DemoServlet.xml*
```xml
<?xml verison='1.0' encoding='utf-8'?>
<context path="/DemoServlet" docBase="E:\Users\Projects\DemoServlet\target\DemoServlet" />
```
* \* 注：target\DemoServlet可通过IDEA的 Build/Build Artifacts... 菜单编译 *
2. **deployWARs** war包
Deploy WARS

3. **deployDirectories** 文件夹
Deploy expanded folders

4. server.xml中配置context

## 流程
```
数据
> 操作系统
> (通过socket获取数据) > 数据解析 [Endpoint > 解析数据]
> (解析成) > Request
> (传递给) > 容器 [ Engine-Pipeline > Host-Pipeline > Context-Pipeline > Wrapper-Pipeline > FilterChain * > Servlet ]

* 此时通过request.getRequest和response.getResponse得到RequestFacade和ResponseFacade对象（分别是Request和Response接口在tomcat中的实现类）
```


## 热部署和热加载

热部署表示重新部署应用，执行主体是Host(主机)
热加载表示重新加载class，执行主体是Context(应用)

#### 热加载
可以通过在Context上配置reloadable属性为true来开启热加载，默认是false
```xml
<Context reloadable="true"></Context>
```
热加载触发的条件是：WEB-INF/classes目录下的文件发生变化，WEB-INF/lib目录下的jar包添加、删除、修改。
