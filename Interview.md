# Interview

## HashMap
1. JDK1.8中对hash算法和寻址算法是如何优化的
JDK1.8以后的Hash算法：
```java
// ^代表亦或运算
static final int hash(Object key){
    int h;
    return (key == null) ? 0:(h=key.hashCode()) ^ (h >>> 16);
}
// 1111 1111 1111 1111 1111 1010 0111 1100
// 0000 0000 0000 0000 1111 1111 1111 1111
// 1111 1111 1111 1111 0000 0101 1000 0011
```
寻址算法：
```java
// n为存储的数组长度
(n-1) & hash
// 0000 0000 0000 0000 0000 0000 0000 1111 (n-1)
// 1111 1111 1111 1111 1111 1010 0111 1100（未优化hash值）
// 1111 1111 1111 1111 0000 0101 1000 0011（优化后hash值）
```
a. 寻址优化：用与运算替代取模，提升性能（当n是2次幂，hash对n取模 和 hash&(n-1)的效果一样）
b. hash优化：对每个hash值，在他的低16位中，让高低16位进行了异或，让他的低16位同时保持了高低16位的特征，尽量避免hash值出现冲突

2. HashMap是如何解决hash冲突问题
a. 冲突时，会在这个位置挂一个链表，让多个key-value对，同时放在数组的一个位置里，寻址时如果发现这个位置挂了一个链表就会遍历，时间复杂度为O(n)。假如链表很长，会把链表转换为红黑树，时间复杂度为O(logn)
3. HashMap是如何进行扩容的
在数组长度为16的时候，他们两个hash值的位置是一样的，用链表来处理，出现一个hash冲突的问题
如果数组的长度扩容之后 = 32，重新对每个hash值进行寻址，也就是用每个hash值跟新数组的length - 1进行与操作.判断二进制结果中是否多出一个bit的1，如果没多，那么就是原来的index，如果多了出来，那么就是index + oldCap，通过这个方式，就避免了rehash的时候，用每个hash对新数组.length取模，取模性能不高，位运算的性能比较高
```java
// n-1        0000 0000 0000 0000 0000 0000 0001 1111
// hash1     1111 1111 1111 1111 0000 1111 0000 0101
// &结果    0000 0000 0000 0000 0000 0000 0000 0101 = 5（index = 5的位置）

// n-1        0000 0000 0000 0000 0000 0000 0001 1111
// hash2     1111 1111 1111 1111 0000 1111 0001 0101
// &结果    0000 0000 0000 0000 0000 0000 0001 0101 = 21（index = 21的位置）
```

## 并发
1. synchronized关键字的底层原理是什么？
2. 聊聊你对CAS的理解以及其底层实现原理

普华永道
1. 在for循环中写一个计时器，先隔2000毫秒打印1，再隔2000毫秒打印2….依次每间隔2000毫秒打印出0到9.
2. vue的生命周期
3. redux的使用
4. css如何实现图标
5. flex的使用(flex-direction, justify-content, align-items, align-self)
