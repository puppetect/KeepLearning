# Tomcat

## 部署

三种方式
1. **deployDescriptors**
Deploy XML descriptors from configBase
e.g * Catalina/localhost/DemoServlet.xml*
```xml
<?xml verison='1.0' encoding='utf-8'?>
<context path="/DemoServlet" docBase="E:\Users\Projects\DemoServlet\target\DemoServlet" />
```
* \* 注：target\DemoServlet可通过IDEA的 Build/Build Artifacts... 菜单编译 *
2. **deployWARs**
Deploy WARS

3. **deployDirectories**
Deploy expanded folders
