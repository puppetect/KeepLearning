# Varnish

## VCL - Varnish Configuration Language

!!![Manual](https://varnish-cache.org/docs/6.3/reference/vcl.html)
!!![Builtin VCL](https://www.varnish-software.com/wiki/content/tutorials/varnish/builtin_vcl.html)
!!![Tutorials](https://docs.varnish-software.com/tutorials/)
!!![Documentation](https://varnish-cache.org/docs)

### VCL Syntax
#### Access control lists (ACLs)
An ACL declaration creates and initializes a named access control list which can later be used to match client addresses:
```c
acl local {
  "localhost";         // myself
  "192.0.2.0"/24;      // and everyone on the local network
  ! "192.0.2.23";      // except for the dialin router
}
```
To match an IP address against an ACL, simply use the match operator:
```c
if (client.ip ~ local) {
  return (pipe);
}
```
#### Operators
Ops | Usage
--- | ---
= | Assignment operator.
== | Comparison.
~ | Match. Can either be used with regular expressions or ACLs.
! | Negation.
&& | Logical and
|| | Logical or

#### Subroutines
A subroutine is used to group code for legibility(清晰) or reusability:
```c
sub pipe_if_local {
  if (client.ip ~ local) {
    return (pipe);
  }
}
```
Subroutines in VCL do not take arguments, nor do they return values.

To call a subroutine, use the call keyword followed by the subroutine's name:
```c
call pipe_if_local;
```

Varnish has quite a few built in subroutines that are called for each transaction as it flows through Varnish. These builtin subroutines are all named vcl_*. Your own subroutines cannot start their name with vcl_.

### Built in subroutines

#### Varnish Processing States
Client Side
![Client Side](https://varnish-cache.org/docs/6.3/_images/cache_req_fsm.svg)
Backend Side
![Backend Side](https://varnish-cache.org/docs/6.3/_images/cache_fetch.svg)

#### vcl_recv
Called at the beginning of a request, after the complete request has been received and parsed, after a restart or as the result of an ESI include.

Its purpose is to decide whether or not to serve the request, possibly modify it and decide on how to process it further. A backend hint may be set as a default for the backend processing side.

#### vcl_pipe
Called upon entering pipe mode. In this mode, the request is passed on to the backend, and any further data from both the client and backend is passed on unaltered until either end closes the connection. Basically, Varnish will degrade into a simple TCP proxy, shuffling bytes back and forth. For a connection in pipe mode, no other VCL subroutine will ever get called after vcl_pipe.

#### vcl_pass
Called upon entering pass mode. In this mode, the request is passed on to the backend, and the backend's response is passed on to the client, but is not entered into the cache. Subsequent requests submitted over the same client connection are handled normally.

#### vcl_hash
Called after vcl_recv to create a hash value for the request. This is used as a key to look up the object in Varnish.

#### vcl_purge
Called after the purge has been executed and all its variants have been evicted.

#### vcl_miss
Called after a cache lookup if the requested document was not found in the cache or if vcl_hit returned fetch.

Its purpose is to decide whether or not to attempt to retrieve the document from the backend. A backend hint may be set as a default for the backend processing side.

#### vcl_hit
Called when a cache lookup is successful. The object being hit may be stale: It can have a zero or negative ttl with only grace or keep time left.

#### vcl_deliver
Called before any object except a vcl_synth result is delivered to the client.

#### vcl_synth
Called to deliver a synthetic object. A synthetic object is generated in VCL, not fetched from the backend. Its body may be constructed using the synthetic() function.

A vcl_synth defined object never enters the cache, contrary to a vcl_backend_error defined object, which may end up in cache.

#### vcl_backend_fetch
Called before sending the backend request. In this subroutine you typically alter the request before it gets to the backend.

#### vcl_backend_response
Called after the response headers have been successfully retrieved from the backend.

#### 304 handling
For a 304 response, Varnish core code amends beresp before calling vcl_backend_response:

#### vcl_backend_error
This subroutine is called if we fail the backend fetch or if max_retries has been exceeded.

#### vcl_init
Called when VCL is loaded, before any requests pass through it. Typically used to initialize VMODs.

#### vcl_fini
Called when VCL is discarded only after all requests have exited the VCL. Typically used to clean up VMODs.

### Request and response VCL objects
objects | definition
--- | ---
req | The request object. When Varnish has received the request the *req* object is created and populated. Most of the work you do in *vcl_recv* you do on or with the *req* object.
bereq | The backend request object. Varnish constructs this before sending it to the backend. It is based on the *req* object.
beresp | The backend response object. It contains the headers of the object coming from the backend. If you want to modify the response coming from the server you modify this object in *vcl_backend_response*.
resp | The HTTP response right before it is delivered to the client. It is typically modified in *vcl_deliver*.
obj | The object as it is stored in cache. Read only.

### 动作
动作 | 含义
--- | ---
pass | 当一个请求被pass后，这个请求将通过varnish转发到后端服务器，该请求不会被缓存，后续的请求仍然通过varnish处理。pass可以在*vcl_recv*中调用.
lookup | 当一个请求在*vcl_hash*中被lookup后，varnish将从缓存中提取数据，如果缓存中没有数据，将被设置为pass
pipe | 和pass相似，都要访问后端服务器，但是当进入pipe模式后，在此连接未关闭前，后续的所有请求都直接发送到后端服务器，不经过varnish处理
deliver | 请求的目标被缓存，然后发送给客户端，一般在*vcl_backend_response*中调用.
hit_for_pass | 表示直接从后台获取数据，会创建一个hit_for_pass对象，该对象的ttl值将会被设置成beresp.ttl的当前值。用来控制*vcl_deliver*如何处理当前的请求，后续的请求会直接*vcl_pass*
hash | 当 *vcl_recv* 返回hash时，会强制从缓存中返回.
fetch | 从后端服务器获取请求目标
restart | 重启本次事务，重新返回给，如果重启次数超过了*max_restarts*报错
retry | 如果对 *vcl_backend_response* or *vcl_backend_error* 返回结果不满意，可重新尝试.

### functions
functions | definition
--- | ---
hash_data(str) | 增加一个散列值，默认hash_data()是调用request的host和url
regsub(str, regex, sub) | 用sub来替换指定的目标
regsuball(str, regex, sub) | 用sub来替换所有发现的目标
ban(expression) | 禁用缓存中url匹配regex的所有对象，这是一种清空缓存中某些无效内容的方法

### http headers
headers | definition
--- | ---
Cache-Control | 制定了缓存如何处理内容。varnish关心max-age参数，并用它来计算对象的ttl。"Cache-Control:no-cache"是被忽略的。
Age | varnish添加了一个Age头信息，以指示在varnish中该对象被保持了多久。可以通过varnishlog抓出Age: `varnishlog -i TxHeader -I ^Age`
Pragma | 一个HTTP 1.0服务器可能会发送"Pragma:no-cache"。varnish忽略这种头信息。在vcl中你可以很方便地增加对这种头信息的支持。在vcl_backend_response中：`if(beresp.http.Pragma ~ "no-cache") { pass; } `
Authorization | varnish看到授权头信息时，它会pass该请求。
Cookie | varnish不会缓存来自后端的具有Set-Cookie
Vary | 是web服务器发送的，代表什么引起了http对象的变化。可以通过Accept-Encoding这样的头信息弄明白。当服务器发出"Vary:Accept-Encoding"，它等于告诉varnish，需要对每个来自客户端的不同Accept-Encoding缓存不同的版本。所以，如果客户端只接收gzip编码，varnish就不会提供deflate编码的页面版本。如果Accept-Encoding字段含有很多不同的编码，比如浏览器这样发送：Accept-Encoding: gzip, deflate 另一个这样发送： Accept-Encoding: deflate, gzip 因为Accept-Encoding头信息不同，varnish将保存两种不同的请求页面。规范Accept-Encoding头信息将确保你的不同请求的缓存尽可能的少。

### Backend servers

### Directors

### Health checks
```c
import directors;

backend server1 {
    .host = "server1.example.com";
    .probe = {
        .url = "/";
        .timeout = 1s; /*定义probe多久检查一次后端，默认是5s*/
        .interval = 5s; /*定义probe的过期时间，默认是2s*/
        .window = 5; /*要检查后端服务器的次数，默认是8*/
        .threshold = 3; /*window里面要有多少次poll成功就认为后端是健康的*/
    }
}

backend server2 {
    .host = "server2.example.com";
    .probe = {
        .url = "/";
        .timeout = 1s;
        .interval = 5s;
        .window = 5;
        .threshold = 3;
    }
}

sub vcl_init {
    new vdir = directors.round_robin();
    vdir.add_backend(server1);
    vdir.add_backend(server2);
}
sub vcl_recv {
    # send all traffic to the bar director:
    set req.backend_hint = bar.backend();
}

```

### Hashing

### Grace mode and keep
#### Grace mode
When several clients are requesting the same page Varnish will send one request to the backend and place the others on hold while fetching one copy from the backend. In some products this is called request coalescing and Varnish does this automatically.


If you are serving thousands of hits per second the queue of waiting requests can get huge. There are two potential problems - one is a thundering herd problem - suddenly releasing a thousand threads to serve content might send the load sky high. Secondly - nobody likes to wait.

Setting an object's grace to a positive value tells Varnish that it should serve the object to clients for some time after the TTL has expired, while Varnish fetches a new version of the object. The default value is controlled by the runtime parameter `default_grace`.

#### Keep
Setting an object's keep tells Varnish that it should keep an object in the cache for some additional time. The reasons to set keep is to use the object to construct a conditional GET backend request (with If-Modified-Since: and/or Ìf-None-Match: headers), allowing the backend to reply with a 304 Not Modified response, which may be more efficient on the backend and saves re-transmitting the unchanged body.

The values are additive, so if grace is 10 seconds and keep is 1 minute, then objects will survive in cache for 70 seconds after the TTL has expired.

#### Setting grace and keep
We can use VCL to make Varnish keep all objects for 10 minutes beyond their TTL with a grace period of 2 minutes:
```
sub vcl_backend_response {
     set beresp.grace = 2m;
     set beresp.keep = 8m;
}
```

## Reporting and statistics
```
varnishlog
-b 只显示varnish和后端服务器之间通信的记录条，当想优化缓存命中率时非常有用
-c 和-b类似，只针对与客户端的通信情况
-i <taglist> 只有显示带有特定标签的行
-I <[taglist:]regex> 通过正则表达式过滤数据

-a 当把日志写入文件时，采用追加的方式，而不是覆盖
-C 匹配正则表达式时，忽略大小写
-d 启动时处理旧日志
-D 以守护进程方式运行
-k <num> 只显示开头的num条记录
-n <dir> 指定varnish实例的名字，用来获取日志，默认是主机名
-r <filename> 从一个文件读取日志，而不是从共享内存中读取
-w <filename> 把日志写到一个文件里，而不是显示他们，如果没有-a参数配合，就会覆盖文件。如果在写文件的时候接收到signup信号，就会创建一个新的文件
-x <taglist> 排除匹配tag的日志
-X <[taglist:]regex> 排除匹配正则表达式的日志

```

默认顺序：
3. **Backend** request
2. **Request** handling until *bereq* started
4. **Request** handling after *bereq* started
1. **Session** start
5. **Session** end

## Varnish and Website Performance

## Content composition with Edge Side Includes

## Footnotes about ESI

## 缓存策略
varnish的默认缓存策略是偏向保守的，只缓存get请求和head请求，不缓存带有cookie和认证信息的请求，也不会缓存带有Set-Cookie或者有变化的头信息的响应。
varnish也会检查请求和响应中的Cache-Control头信息，这个头信息中会包含一些选项来控制缓存行为。当Cache-control中max-age的控制和默认策略冲突时，varnish不会单纯根据cache-control信息就改变自己的缓存行为。
例如：cache-control：max-age=n，n为数字，如果varnish收到web服务器的响应中包含max-age，varnish会以此值设定缓存的过期时间，否则varnish将会设置为参数配置的时间，默认为120s。

提高varnish命中率的根本方法，就是仔细规划请求和应答，并自定义缓存策略，通过vcl来配置自己想要缓存的内容，并主动设置对象的ttl，尽量不去依赖http header。

当然，如果使用默认策略的话，就需要好好跟踪和分析http header了。

有一个提高命中的简单方法，就是尽量加大ttl值，当然要在合理范围.
查看日志，分析经常访问后端服务器的url，有一些常用命令比如：`varnishtop -i BereqURL`
