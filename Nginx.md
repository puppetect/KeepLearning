# Nginx

!!![Documentation](http://nginx.org/en/docs/)

## QuickStart

#### Starting, Stopping, and Reloading Configuration
To start nginx, run the executable file
nginx -s *signal*
- stop — fast shutdown
- quit — graceful shutdown
- reload — reloading the configuration file
- reopen — reopening the log files

#### Configuration File’s Structure

nginx consists of modules which are controlled by directives specified in the configuration file. Directives are divided into simple directives and block directives. A simple directive consists of the name and parameters separated by spaces and ends with a semicolon (;). A block directive has the same structure as a simple directive, but instead of the semicolon it ends with a set of additional instructions surrounded by braces ({ and }). If a block directive can have other directives inside braces, it is called a context (examples: events, http, server, and location).

Directives placed in the configuration file outside of any contexts are considered to be in the main context. The **events** and **http** directives reside in the main context, server in http, and location in server.

The rest of a line after the # sign is considered a comment.

#### Serving Static Content

Generally, the configuration file may include several server blocks distinguished by ports on which they listen to and by server names. Once nginx decides which server processes a request, it tests the URI specified in the request’s header against the parameters of the location directives defined inside the server block.
This location block specifies the “/” prefix compared with the URI from the request. For matching requests, the URI will be added to the path specified in the root directive
```
http {
    server {
        location / {
            root /data/www;
        }
        location /images/ {
            root /data;
        }
    }
}
```
#### Setting Up a Simple Proxy Server

In the first location block, put the proxy_pass directive with the protocol, name and port of the proxied server specified in the parameter (in our case, it is http://localhost:8080)

The parameter in location block is a regular expression matching all URIs ending with .gif, .jpg, or .png. A regular expression should be preceded with ~. The corresponding requests will be mapped to the /data/images directory.
```
server {
    location / {
        proxy_pass http://localhost:8080;
    }

    location ~ \.(gif|jpg|png)$ {
        root /data/images;
    }
}
```
When nginx selects a location block to serve a request it first checks location directives that specify prefixes, remembering location with the longest prefix, and then checks regular expressions. If there is a match with a regular expression, nginx picks this location or, otherwise, it picks the one remembered earlier.

## 入门

#### 基本配置
默认启动nginx时使用的配置文件是：安装路径/conf/nginx.conf文件。可以在启动时通过`-c`指定要读取的配置文件。
常见的配置文件如下：
- nginx.conf： 应用程序的基本配置文件
- mime.types： mime类型关联的扩展文件
- fastcgi.conf： 与fastcgi相关的配置（与php有关）
- proxy.conf： 与proxy相关的配置，也可直接在nginx.conf中配置
- sites.conf： 配置nginx提供的网站，包括虚拟主机，也可直接在nginx.conf中配置
nginx的进程结构：启动nginx时会启动一个master进程，这个进程不处理任何客户端请求，主要用来产生worker进程，一个worker进程用来处理一个请求。
nginx模块分为：核心模块、事件模块、标准http模块、可选http模块、邮件模块、第三方模块和补丁等

#### Http模块

**Location区段**
通过指定模式与客户端请求的URI相匹配，基本语法如下：
```
location [=|~|~*|^~|@] pattern{...}
```
修饰符 | 涵义
--- | ---
无 | 必须以指定模式开头
`=` | 必须与指定模式精确匹配（?参数可忽略不计）
`~` | 匹配正则表达式要区分大小写
`~*` | 匹配正则表达式不区分大小写
`^~` | 类似无修饰行为，也是以指定模式开头，不同的是，如果模式匹配，则停止搜索其他模式
`@` | 定义命名location区段，这些区段客户端不能访问，只能由内部产生的请求来访问，如try_files或error_page等

查找顺序和优先级
1. 带有=的精确匹配优先
2. 没有修饰符的精确匹配
3. 正则表达式按照他们在配置文件中定义的顺序
4. 带有^~修饰符的，开头匹配
5. 带有~或~*修饰符的，如果正则表达式与uri匹配
6. 没有修饰符的，如果指定字符串与uri开头匹配


**Listen区段**
指定了可以被访问到的ip地址和端口号，可以只指定一个ip，一个端口，或者一个可解析的服务器名。最常用的是配置端口

#### 反向代理

