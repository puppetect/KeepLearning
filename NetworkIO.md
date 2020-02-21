# BIO/NIO

## 原理

#### BIO
*BIOServer.java*
```java
public class BIOServer {
    static byte[] bytes = new byte[1024]
    public static void main(String[] args){
        try{
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(8080));

            while(true){
                // blocking
                Socket socket = serverSocket.accept();
                // blocking
                int readBytes = socket.getInputStream().read(bytes);
                String content = new String(bytes);
                System.out.println(content);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
```
*BIOClient.java*
```java
public class Client{
    public static void main(String[] args){
        try{
            Socket socket = new Socket("127.0.0.1", 8081);
            socket.connect(new InetSocketAddress(8080));
            socket.getOutputStream().write("xxx".getBytes());
        }c atch (IOException e){
            e.printStackTrace();
        }
    }
}
```
#### 缺点
- 在不考虑多线程情况下，bio无法处理并发。
- 如果开很多线程，碰到连接上但不活跃的时候，依然会阻塞在read阶段，浪费cpu资源

#### 改进
单线程处理并发(多路复用：多个网络连接复用同一个线程)
```java
public class BIOServer {

    static byte[] bytes = new byte[1024];

    static List<SocketChannel> socketChannelList = new ArrayList<SocketChannel>()；

    public static void main(String[] args){
        try{
            ServerSocketChannel serverSocketChannel = new ServerSocketChannel();
            serverSocketChannel.bind(new InetSocketAddress(8080));
            // set serverSocket non-blocking
            serverSocketChannel.configureBlocking(false);
            while(true){

                // non-blocking operation
                SocketChannel socketChannel = serverSocket.accept();
                if(socketChannel == null){
                    // continue
                } else {
                    // set socket non-blocking
                    socket.setConfig(false);
                    list.add(socket);
                }

                for(SocketChannel channel: socketChannelList){
                    // non-blocking operation
                    ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                    int readBytes = channel.read(byteBuffer);

                    if(readByes != 0){
                        byteBuffer.flip(); //写->读
                        System.out.println(new String(byteBuffer.array()));
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
```
缺点：依然解决不了不活跃线程浪费cpu的问题，对每个线程是否接收数据作轮询太耗资源

#### 再改进
将上面for循环读数据的操作交给操作系统处理，操作系统会调用相关函数。比如linux的epoll和windows的select。

*redis底层选用IO通信模型也是用到了epoll，epoll当有通信来的时候可以直接定位到那个socket，而不需要循环。所以比select效率高，能支持高并发高可用

#### NIO
> Non-blocking io uses a single thread or only a small number of multi-threads. Each connection shares a single thread. Thread resources can be released to handle other requests while waiting (without events). The main thread allocates resources to handle related events by notifying (waking up) the main thread through event-driven model when events such as accept/read/write occur. java.nio.channels.Selector is the observer of events in this model. It can register multiple events of SocketChannel on a Selector. When no event occurs, the Selector is blocked and wakes up the Selector when events such as accept/read/write occur in SocketChannel.(*[Analysis of NIO selector principle](https://programmer.ink/think/analysis-of-nio-selector-principle.html)*)

![NIO Diagram](https://programmer.ink/images/think/108539a1a691325e31b8f18a04e2a52d.jpg)

```java
selector = Selector.open();

ServerSocketChannel ssc = ServerSocketChannel.open();
ssc.configureBlocking(false);
ssc.socket().bind(new InetSocketAddress(port));

ssc.register(selector, SelectionKey.OP_ACCEPT);

while (true) {

    // select() block, waiting for an event to wake up
    int selected = selector.select();

    if (selected > 0) {
        Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
        while (selectedKeys.hasNext()) {
            SelectionKey key = selectedKeys.next();
            if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                // Handling accept events
            } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                // Handling read events
            } else if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
                // Handling write events
            }
            selectedKeys.remove();
        }
    }
}
```

