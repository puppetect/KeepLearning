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

## 基础

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

#### Http Core模块

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

#### Http Rewrite模块
用来执行url重定向，有利于去掉恶意访问的url，也有利于搜索引擎优化（seo）。
nginx使用的语法源于Perl兼容正则表达式库(PCRE)，基本语法如下：
符号 | 表示
--- | ---
`^` | 必须以^后的实体开头
`$` | 必须以$前的实体结尾
`.` | 匹配任何字符
`[]` | 匹配指定字符集内的任意字符
`[^]` |匹配任何不包括在指定字符集内的任意字符串
`|` | 匹配`|`前或之后的实体
`()` | 分组，组成一组用于匹配的实体，通常会有`|`协助

捕获子表达式，可以捕获放在()间的任何文本，比如：
`^(.*)(hello|sir)$` 字符串为`"hi sir"` 捕获结果： `$1=hi $2=sir` 这些被捕获的数据，在后面就可以当变量一样使用

if条件结构的基本语法：
1. 没有操作符：当指定的字符串或者变量不为空，也不为0开始的字符串，取true
2. `=`, `!=`，例：`if($request_method = POST){...}`
3. `~`, `~*`, `!~`, `!~*`，例： `if($uri ~* "\.jsp$"){...}`
4. `-f`,`!-f`: 用来测试指定文件是否存在，例如：`if(-f $request_filename){...}`
5. `-d`,`!-d`: 用来测试指定目录是否存在
6. `-e`,`!-e`: 用来测试指定文件、目录或者符号链接是否存在
7. `-x`,`!-x`: 用来测试指定文件是否存在和是否可以执行
8. `break`: 跳出if块
9. `return`: 终止处理，并返回一个指定的http状态码（可用204, 400, 402-406, 408, 410, 411, 413, 416与500-504）
10. `set`: 初始化或者重定义一个变量

**rewrite**
`rewrite regex replacement flag`
flag | 含义
--- | ---
last | 完成重写指令，之后搜索相应的url或location
break | 完成重写指令，在location中使用，否则nginx会执行10次循环并返回500错误
redirect | 返回302临时重定向，如果替换字段用http://开头则被禁用
permanent | 返回301永久重定向

如果替换的字段中包含参数，那么参数将附加到replacenment后面，为了防止附加，可以在最后一个字符后面加一个问号：
`rewrite ^/users/(.*)$ /show?user=$1? last;`

#### Gzip模块
```
gzip            on;
gzip_min_length 1000;
gzip_proxied    expired no-cache no-store private auth;
gzip_types      text/plain application/xml;
```


#### Memcached模块
把Nginx当作Memcached客户端，用来连接Memcached的模块。

#### Http Limit Zone模块
用于会话的连接数控制，如限制每个ip的并发连接数等

#### Http Referer模块
用于防盗链

#### Http Browser模块
按照请求头中的`User-agent`来创建一些变量，好为不同浏览器创建不同的内容

## 配置
**优化方向和目标**
1. 尽量提高单台机器处理效率
2. 尽量降低单台机器的负载
3. 尽量降低磁盘I/O
4. 尽量降低网络I/O
5. 尽量减少内存使用
6. 尽量高效利用CPU

**措施**
1.用户和组：在生产环境下，最好是专为nginx创建用户和组，并单独设置权限，更安全。例如 `user nginx nginx`
2.worker_processes：通常配置成cpu的总核数，或者其2倍，性能会更好。这可以减少进程间切换带来的消耗
3.可以同时使用worker_cpu_affinity来绑定cpu，使得每个worker进程独享一个cpu，实现完全的并发，性能更好，不过这只对linux有效
4.event里事件模型，linux推荐用epoll，freeBSD推荐用kqueue
5.worker_rlimit_nofile: 描述一个nginx进程最多打开的文件数目。配置成跟linux内核下文件打开数一致即可。通过`ulimit -n`来查看。
6.worker_connections:每个进程允许的最多连接数，默认是1024，可以大一些。
7.keepalive_timeout: 配置成65左右即可
8.client_header_buffer_size:设置请求的缓存，通常为系统分页大小的整数倍(4k)，可以通过`getconf PAGESIZE`来查看系统分页大小
9.对打开文件设置缓存
open_file_cache_max=建议设置成和每个进程打开的最大文件数一致 inactive=60s;
open_file_cache_valid 90s;
open_file_cache_min_users 2;
open_file_cache_errors on;
10. 尽量开启Gzip压缩，gzip_comp_level通常设置成3-5，高了浪费cpu
11. error日志优化：运行期间设置为crit，可以减少I/O
12. access日志优化：如果使用了其他统计软件，可以关闭日志，减少磁盘写，或者写入内存文件，提高I/O效率
13. sendfile指令指定nginx是否调用sendfile函数来输出文件，通常应设置为on，如果是下载等应用磁盘io重负载应用，可设置为off
14. buffer size优化：如果buffer size太小就会导致nginx使用临时文件存储response，这会引起磁盘读写io，流量越大问题越明显。
    client_body_buffer_size 处理客户端请求体buffer大小。用来处理post提交数据，上传文件等。它需要足够大以容纳需要上传的post数据。同时还有后端的buffer数据。
15. worker_priority进程优先级设置：linux系统中，优先级高的进程会占用更多的系统资源，这里配置的是进程的静态优先级，取值范围-20到+19，-20级别最高。因此可以把这个值设置小一点，但不建议比内核进程的值低(通常为-5)
16. 合理设置静态资源的浏览器缓存时间，尽量用浏览器缓存
17. 负载均衡锁accept_mutex，建议开启，默认就是开启的
18. 如果使用ssl的话，而且服务器上有ssl硬件加速设备的话，请开启硬件加速

## 压力测试
```
ab -n1000 -c10 http://localhost
```
