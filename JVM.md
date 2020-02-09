# JVM

## 基本结构

JVM由三个主要的子系统构成
- 类加载子系统
- 运行时数据区（内存结构）
- 执行引擎

运行时数据区
公共：
    方法区(Method Area) 静态变量、常量、类信息（构造方法/接口定义）、运行时常量池
    堆(Heap) 存放对象实例
私有：
    虚拟机栈
    本地方法栈
    程序计数器

栈帧
1. 局部变量表（存储方法局部变量和引用类型）
2. 操作数栈（所有操作都在此执行）
3. 动态链接 （把符号引用转化成直接引用）
4. 方法出口（指向方法调用者的内存地址）

## 调优工具

Jinfo 查看正在运行的java程序的扩展参数和系统属性
查看jvm的参数
```
jinfo -flags 进程id
```
查看java系统属性  等同于System.getProperties()
```
jinfo -syspros 进程id
```

Jstat 查看堆内存各部分的使用量，以及加载类的数量。
```
jstat [-命令选项][vmid][间隔时间/毫秒][查询次数]
如 jstat -gc 进程id
```

Jmap 查看内存信息
堆的对象统计
```
jmap -histo 进程id > xxx.txt
```
堆内存dump
```
jmap -dump:format=b,file=temp.hprof
```
也可以设置在内存溢出时自动导出dump文件（内存过大可能会导不出来）
1. -XX:+HeapDumpOnOutOfMemoryError
2. -XX:HeapDumpPath=输出路径
```
-Xms10m -Xmx10m -XX:+PirntGCDetails -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=d:\oomdump.dump
```

Jstack 生成java虚拟机当前时刻的线程快照

