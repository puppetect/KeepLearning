# BIO/NIO

## BIO

#### 示例
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
单线程处理并发
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

                for(SocketChannel sc: socketChannelList){
                    // non-blocking operation
                    ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                    int readBytes = socket.getInputStream().read(bytes);

                    if(readByes != -1){
                        byteBuffer.flip(); //写-》读
                        String content = Charset.forName("utf-8").decode(byteBuffer);
                        System.out.println(content);
                        byteBuffer.clear();
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
```
缺点：依然解决不了不活跃线程浪费cpu的问题
