# Docker

## 背景
开发和运维因为环境不同而导致的矛盾
集群环境下每台机器部署相同的应用
DevOps(Development and Operations)

## 架构
Docker使用C/S架构，client通过接口与server进程通信实现容器的构建、运行和发布

**Image 镜像**
将软件环境打包好的模板，用来创建容器，一个镜像可以创建多个容器。
镜像分层结构：
```
Container
Image
Parent Image
Base Image
BootFS
```
位于下层的镜像称为父镜像(parent image)，最底层的称为基础镜像(base image)，最上层为“可读写”层，其下的均为“只读”层。
底层应用了Linux的LXC(Linux容器技术), namespace(资源隔离), cgroup(资源限制), AUFS(联合挂载文件系统)
AUFS:
- advanced multi-layered unification filesystem: 高级多层统一文件系统
- 用于为Linux文件系统实现“联合挂载”
- AUFS是之前的UnionFS的重新实现
- Docker最初使用AUFS作为容器文件系统层
- AUFS的竞争产品是overlayFS，从3.8开始被并入Linux内核
- Docker的分层镜像，除了AUFS，Docker还支持btrfs, devicemapper和vfs等

**Container 容器**
Docker的运行组件，启动一个镜像就是一个容器，容器与容器之间相互隔离，互不影响。

**Docker Client**
Docker命令行工具，用户是用Docker Client与Docker daemon进行通信并返回结果给用户，也可以使用其他工具通过Docker api与Docker daemon通信

**Registry 仓库服务注册**
经常会和仓库(Repository)混为一谈，实际上Registry可以有很多仓库，每个仓库可以看成一个用户，一个用户的仓库存放了多个镜像。仓库分为公开仓库(Public Repository)和私有仓库(Private Repository)，最大的公开仓库是官方的Docker Hub

## 命令
**运行容器**
docker run --name 容器名 -i -t -p 主机端口:容器端口 -d -v 主机目录:容器目录:ro 镜像id或镜像名:标签
--name 指定容器名，可自定义，不指定自动命名
-i 以交互模式运行容器
-t 分配一个伪终端，即命令行，通常-it组合使用
-p 指定映射端口，将主机端口映射到容器内的端口
-d 后台运行容器
-v 指定挂载主机目录到容器目录，默认为rw读些模式，ro表示只读模式

**容器列表**
```
docker ps -a -q
```
-a 查看所有容器（运行中、未运行）
-q 只查看容器的id

**启动容器**
```
docker start 容器id或容器名
```

**删除容器**
```
docker rm -f 容器id或容器名
```
-f 强制删除

**查看日志**
```
docker logs 容器id或容器名
```

**进入正在运行的容器**
```
docker exec -it 容器id或容器名 /bin/bash
```
进入正在运行的容器并且开启交互模式终端
/bin/bash是固有写法，作用是因为docker后台必须运行一个进程，否则容器就会退出，在这里表示启动容器后启动bash。

**拷贝文件**
主机中文件拷贝到容器中
```
docker cp 主机文件路径 容器id或容器名:容器路径
```
容器中文件拷贝到主机中
```
docker cp 容器id或容器名:容器路径 主机文件路径
```

**获取容器元信息**
```
docker inspect 容器id或容器名
```

## Demo
```
docker pull mysql
docker network create net
docker run --name mysql -p 33060:3306 -e MYSQL_ROOT_PASSWORD=123456 -d --net=net mysql
docker cp /Users/andong/Docker/mysql/20200210_hzd-remote.sql mysql:/var/hzd.sql
docker exec -it mysql bash
```

```
mysql -h localhost -u root -p（进入mysql下面）
create database hzd;(创建数据库)
show databases;(就可看到所有已经存在的数据库，以及刚刚创建的数据库abc)
use hzd;(进入abc数据库下面)
show tables;(产看abc数据库下面的所有表,空的)
source /var/hzd.sql（导入数据库表）
show tables;(查看abc数据库下面的所有表,就可以看到表了)
desc hzd;(查看表结构设计)
select * from ;
exit;
GRANT ALL ON *.* TO 'root'@'%';
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456' PASSWORD EXPIRE NEVER;
flush privileges;
```

```
jar -xvf hzd.war
jar -cvf hzd.war .
```
```
jdbc_url=jdbc\:mysql\://mysql\:3306/hzd?useUnicode\=true&characterEncoding\=UTF-8&zeroDateTimeBehavior\=convertToNull
jdbc_username=root
jdbc_password=123456
```
```
docker pull tomcat
docker run --name tomcat -d -v /Users/andong/Docker/tomcat/hzd.war:/usr/local/tomcat/webapps/hzd.war -p 8080:8080 --net=net tomcat
docker cp /Users/andong/Downloads/Desktop/upload tomcat:/usr/local/tomcat/webapps/hzd/upload
docker cp /Users/andong/Projects/Java/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/hzd-web/WEB-INF/views/desktop/index.jsp tomcat:/usr/local/tomcat/webapps/hzd/WEB-INF/views/desktop/index.jsp
docker cp /Users/andong/Projects/Java/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/hzd-web/WEB-INF/views/desktop/help/index.jsp tomcat:/usr/local/tomcat/webapps/hzd/WEB-INF/views/desktop/help/index.jsp
docker cp /Users/andong/Projects/Java/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/hzd-web/WEB-INF/views/mobile/index.jsp tomcat:/usr/local/tomcat/webapps/hzd/WEB-INF/views/mobile/index.jsp
docker cp /Users/andong/Projects/Java/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/hzd-web/WEB-INF/lib/hzd.jar tomcat:/usr/local/tomcat/webapps/hzd/WEB-INF/lib/hzd.jar
```
```
docker pull varnish
docker run --name varnish -v /Users/andong/Docker/varnish/default.vcl:/etc/varnish/default.vcl:ro --tmpfs /usr/local/var/varnish:exec -d -p 6081:80 --net=net varnish
```
```
docker pull nginx
docker run --name nginx -d -v /Users/andong/Docker/nginx/nginx.conf:/etc/nginx/nginx.conf -p 80:80 --net=net nginx
```
