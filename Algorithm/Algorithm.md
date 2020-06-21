# Algorithm

## 常用数据结构和技巧

- 数组、字符串 / Array & String

优点
构建一个数组非常简单
能让我们在O(1)的时间里根据数组的下标(index)查询某个元素
缺点
构建时必须分配一段连续的空间
查询某个元素是否存在时需要遍历整个数组，耗费O(n)的时间（其中，n是元素的个数）
例题
Leetcode #242

- 链表 / Linked list
优点
灵活地分配内存空间
能在O(1)时间内删除或者添加元素
缺点
查询元素需要O(n)时间
解题技巧
利用快慢指针（有时候需要用到三个指针）
构建一个虚假链表头
例题
Leetcode #25 K个一组翻转链表

- 栈 / Stack
算法基本思想
可以用一个单链表来实现
只关心上一次的操作
处理完上一次的操作后，能在O(1)时间内查找到更新一次的操作
例题
Leetcode #20 #739

- 队列 / Queue
常用的场景
广度优先搜索

- 双端队列 / Deque
基本实现
可以利用一个双链表
队列的头尾两端能在O(1)的时间内进行数据的查看、添加和删除
常用的场景
实现一个长度动态变化的窗口或者连续区间
例题
Leetcode #239

- 树 / Tree
树的共性
结构直观
通过树问题来考察 递归算法 掌握的熟练程度

面试中常考的树的形状有
普通二叉树
平衡二叉树
完全二叉树
二叉搜索树🌟
四叉树
多叉树
特殊的树：红黑树、自平衡二叉搜索树

遍历
前序、中序、后序
例题
Leetcode #230

## 高级数据结构

- 优先队列 / Priority Queue
与普通队列的区别
保证每次取出的元素是队列中优先级最高的
优先级别可自定义
最常用的场景
从杂乱无章的数据中按照一定的顺序（或者优先级）筛选数据
O(nlogn) -> O(k + nlogk)
本质
二叉堆的结构，堆在英文里叫Binary Heap
利用一个数组结构来实现完全二叉树
特性
数组里的第一个元素array[0]有最高的优先级
给定一个下标i，那么对于元素array[i]而言
父节点对应的元素下标是(i-1)/2
左侧子节点对应的元素下标是2*i+1
右侧子节点对应的元素下标是2*i+2
数组中每个元素的优先级都必须要高于它两侧子节点
基本操作
向上筛选 (sift up / bubble up) O(logk)
向下筛选 (sift down / bubble down) O(logk)
另一个最重要的时间复杂度：优先队列的初始化 O(n)
例题
Leetcode #347

- 图 / Graph
最基本知识点如下
阶、度
树、森林、环
有向图、无向图、完全有向图、完全无向图
连通图、连通分量
图的存储和表达方式：邻接矩阵、邻接链表
围绕图的算法
图的遍历：深度优先、广度优先
环的检测：有向图、无向图
拓扑排序
最短路径算法：Dijkstra、Bellman-Ford、Floyd Warshall
连通性相关算法：Kosaraju、Tarjan、求解孤岛的数量、判断是否为树
图的着色、旅行商问题

必须熟练掌握的知识点
图的存储和表达方式：邻接矩阵、邻接链表
🌟图的遍历：深度优先、广度优先
二部图的检测(Bipartite)、树的检测、环的检测：有向图、无向图
拓扑排序
联合-查找算法(Union-Find)
最短路径算法：Dijkstra、Bellman-Ford
例题
Leetcode #785

- 前缀树 / Trie
也称字典树，这种数据结构被广泛运用在字典查找中
重要性质
每个节点至少包含两个基本属性
children: 数组或者集合，罗列出每个分支当中包含的所有字符
isEnd: 布尔值，表示该节点是否为某字符串的结尾
跟节点是空的
除了根节点，其他所有节点都有可能是单词的结尾，叶子节点一定都是单词的结尾
最基本的操作
创建
方法
遍历一遍输入的字符串，对每个字符串的字符进行遍历
从前缀树的根节点开始，将每个字符加入到节点的children字符集当中
如果字符集已经包含了这个字符，跳过
如果当前字符是字符串的最后一个，把当前节点的isEnd标记为真
搜索
方法
从前缀树的根节点出发，逐个匹配输入的前缀字符
如果遇到了，继续往下一层搜索
如果没遇到，立即返回
例题
Leetcode #212

- 线段树 / Segment Tree
一种按照二叉树的形式存储数据的结构，每个节点保存的都是数组里某一段区间的总和 O(nlogn)
例题
Leetcode #315

- 树状数组 / Fenwick Tree / Binary Indexed Tree
重要的基本特征
利用数组来表示多叉树的结构，和优先队列有些类似
优先队列使用数组来表示完全二叉树，而树状数组是多叉树
树状数组的第一个元素是空节点
如果节点tree[y]是tree[x]的父节点，那么需要满足y=x-(x&(-x))
例题
Leetcode #308

## 排序算法
基本的排序算法
- 冒泡排序 / Bubble Sort
O(n^2)
- 插入排序 / Insertion Sort
```java
void sort(int[] nums){
    for (int i=1, current; i<nums.length; i++){
        current = nums[i];
        for(int j=i-1; j>=0 && nums[j] > current; j--){
            nums[j+1] = nums[j]
        }
        nums[j+1] = current;
    }
}
```
O(n^2)
例题
Leetcode #147

常考的排序算法
- 归并排序 / Merge Sort
```java
void sort(int[] A, int lo, int hi){
    if(lo >= hi) return;
    int mid = lo + (hi - lo)/2;

    sort(A, lo, mid);
    sort(A, mid + 1, hi)
    merge(A, lo, mid, hi)
}

void merge(int[] nums, int lo, int mid, int hi){
    int[] copy = nums.copy();
    int k = lo, i = lo, j = mid + 1;

    while(k <= hi){
        if(i > mid){
            nums[k++] = copy[j++];
        } else if(j > hi){
            nums[k++] = copy[i++];
        } else if(copy[j] < copy[i]){
            nums[k++] = copy[j++];
        } else {
            nums[k++] = copy[i++];
        }
    }
}
```
时间复杂度O(nlogn)
空间复杂度O(n)
- 快速排序 / Quick Sort
```java
void sort(int[] nums, int lo, int hi){
    if(lo>=hi) return;

    int p = partition(nums, lo, hi);

    sort(nums, lo, p-1);
    sort(nums, p+1, hi);
}

void partition(int[] nums, int lo, int hi){
    swap(nums, randRange(lo, hi), hi);

    int i, j;

    for(int i = lo, j=lo; j < hi; j++){
        if(nums[j] < nums[hi]){
            swap(nums, i++, j);
        }
    }
    swap(nums, i, j);
}
```
时间复杂度O(nlogn)
空间复杂度O(logn)
例题
Leetcode #215
- 拓扑排序 / Topological Sort
应用场合
拓扑排序就是要将图论里的顶点按照相连的性质进行排序
前提
- 必须是有向图
- 图里没有环
广度优先解法：
```java
void sort(){
    Queue<Integer> q = new LinkedList();

    for(int v=0; v<V; v++){
        if(indegree[v] == 0) q.add(v);
    }

    while (!q.isEmpty()){
        int v = q.poll();
        print(v);
        for (int u=0; u<adj[v].length; u++){
            if(--indegree[u] == 0){
                q.add(u);
            }
        }
    }
}
```
时间复杂度O(n)

其他排序算法
- 堆排序 / Heap Sort
- 桶排序 / Bucket Sort

## 递归和回溯 / Recursion & Backtracking
递归
汉诺塔
```java
void hano(char A, char B, char C, int n){
    if (n > 0){
        hano(A, C, B, n-1);
        move(A, C);
        hano(B, A, C, n-1);
    }
}
```
Leetcode #91 #247

递归写法结构
```javascript
function fn(n){
    // 第一步：判断输入或者状态是否非法？如果非法立即返回，也称为完整性检查(Sanity Check)
    if(input/state is invalid){
        return;
    }
    // 第二步：判断递归是否应当结束?
    if(match condition){
        return some values;
    }
    // 第三步：缩小问题规模
    result1 = fn(n1)
    result2 = fn(n2)
    ...
    // 第四步：整合结果
    return combine(result1, result2)
}
```
时间复杂度
T(n) = a * T(n/b) + f(n)
当参数a,b都确定时，只看递归部分，时间复杂度就是：O(n^log<sub>b</sub>a)

- 当递归部分的执行时间O(n^log<sub>b</sub>a)`>`f(n)的时候，最终的时间复杂度就是O(n^log<sub>b</sub>a)
- 当递归部分的执行时间O(n^log<sub>b</sub>a)`<`f(n)的时候，最终的时间复杂度就是f(n)
- 当递归部分的执行时间O(n^log<sub>b</sub>a)`<`f(n)的时候，最终的时间复杂度就是O(n^log<sub>b</sub>a)logn


回溯

Leetcode #39 #52
解决问题的套路
```javascript
function fn(n){
    // 第一步：判断输入或者状态是否非法？如果非法就立即返回
    if(input/state is invalid){
        return;
    }
    // 第二步：判断递归是否应当结束
    if(match condition){
        return some value;
    }
    // 遍历所有可能出现的情况
    for(all possible cases){
        // 第三步：尝试下一步的可能性
        solution.push(case)
        // 递归
        result = fn(m)
        // 第四步：回溯到上一步
        solution.pop(case)
    }
}
```

## 深度优先和广度优先 DFS&BFS

深度优先搜索
DFS解决的是连通性的问题，即给定了一个起始点（或某种起始状态）和一个终点（或某种最终状态）
判断是否有一条路径能从起点连接到终点。
很多情况下，连通的路径有很多条，只需要找出一条即可，DFS只关心路径存在与否，不在乎其长短

如何对图进行深度优先遍历？
1. 深度优先遍历必须依赖栈(Stack)这个数据结构
2. 栈的特点是后进先出(LIFO)

DFS的递归实现
利用递归去实现DFS可以让代码看上去很简洁
递归的时候需要将当前程序中的变量以及状态压入到系统的栈中
压入和弹出栈都需要较多的时间，如果需要压入较深的栈，会造成运行效率低下

DFS的非递归实现
栈的数据结构也支持压入和弹出操作
完全可以利用栈来提高运行效率

DFS复杂度分析
由于DFS是图论里的算法，分析利用DFS解题的复杂度时，应当借用图论的思想，图有两种表示方式：
- 邻接表（图里有V个顶点，E条边）
访问所有顶点的时间为O(V)，查找所有顶点的邻居的时间为O(E)，所以总的时间复杂度为O(V+E)
- 邻接矩阵（图里有V个顶点，E条边）
查找每个顶点的邻居需要的时间为O(V)，所以整个矩阵的时间复杂度为O(V<sup>2</sup>)

广度优先搜索
一般用来解决最短路径的问题
广度优先搜索是从起始点出发，一层一层进行
每层当中的点距离起始点的步数都是相同的

双端BFS
同时从起始点和终点开始进行的广度优先搜索成为双端BFS
双端BFS可以大大地提高搜索的效率
例如，想判断社交应用程序中两个人之间需要经过多少朋友介绍才能互相认识

如何对图进行广度优先遍历？
1. 广度优先遍历必须依赖队列(Queue)这个数据结构
2. 队列的特点是先进先出(FIFO)

BFS复杂度分析
由于BFS是图论里的算法，分析利用BFS解题的复杂度时，应当借用图论的思想，图有两种表示方式：
- 邻接表（图里有V个顶点，E条边）
每个顶点都要被访问一次，时间复杂度为O(V)，在访问每个顶点的时候，与它相连的顶点（也就是每条边）也要被访问一次，加起来就是O(E)，所以总的时间复杂度为O(V+E)
- 邻接矩阵（图里有V个顶点，E条边）
由于有V个顶点，每次都要检查每个顶点与其他顶点是否有联系，因此时间复杂度为O(V<sup>2</sup>)


## 动态规划

一种数学优化的方法，也是编程的方法
基本属性
- 最优子结构 Optimal Substructure
状态转移方程f(n)
- 重叠子问题 Overlapping Sub-problems


解决动态规划问题最难的两个地方
- 如何定义f(n)
- 如何通过f(1), f(2), ..., f(n-1)推导出f(n)，即状态转移方程
Leetcode #300

题目分类

1. 线性规划 Linear Programming
- 各个子问题的规模以线性的方式分布
- 子问题的最佳状态或结果可以存储在一维线性的数据结构中，如：一维数组，哈希表等
- 通常我们会用dp[i]表示第i个位置的结果，或者从0来开始到第i个位置为止的最佳状态或结果

基本形式
- 当前所求的值仅仅依赖于有限个先前计算好的值，即dp[i]仅仅依赖于有限个dp[j]，其中`j<i`
- 当前所求的值仅仅依赖于所有先前计算和的值，即dp[i]是各个dp[j]的某种组合，其中j由0遍历到i-1

Leetcode #70 #198 #62

2. 区间规划 Interval Programming
- 各个子问题的规模由不同区间来定义
- 子问题的最佳状态或结果可以存储在二维数组中
- 这类问题的时间复杂度一般为多项式时间，即对于一个大小为n的问题，时间复杂度不会超过n的多项式倍数

Leetcode #516

3. 约束规划 Constraint Programming
非决定性多项式 Non-derterministic Polynomial
非决定性多项式时间复杂度
指数级复杂度，如O(2<sup>n</sup>)
全排列算法，复杂度为O(n!)
多项式时间复杂度
O(1), O(n), O(nlogn), O(n<sup>2</sup>)等

解题思想
算法复杂度

## 二分搜索 Binary Search
定义
二分搜索也称折半搜索，是一种在有序数组中查找某一特定元素的搜索算法

运用前提
- 数组必须是排好序的
- 输入并不一定是数组，也可能是给定的一个区间的起始和终止的位置

优点
- 二分搜索也称对数搜索，其时间复杂度为O(lgn)，是一种非常高效的搜索

缺点
- 要求待查找的数组或区间是排好序的
    - 若要求对数组进行动态地删除和插入操作并完成查找，平均时间复杂度会变为O(n)
    - 采取自平衡的二叉查找树
        - 可在O(nlogn)的时间内用给定的数据构建出一颗二叉查找树
        - 可在O(logn)的时间内对数据进行搜索
        - 可在O(logn)的时间内完成删除和插入操作
当：输入的数组或区间是有序的，且不会常变动，要求从中找出一个满足条件的元素，宜采用二分搜索

基本解题模板
- 递归
    - 优点是简洁
    - 缺点是执行开销大
```java
int binarySearch(int[] nums, int target, int low, int high){
    if(low > high) return -1;
    int middle = low + (high - low) / 2;

    if(nums[middle] == target){
        return middle;
    }
    if(target < nums[middle]){
        return binarySearch(nums, target, low, middle - 1);
    } else {
        return binarySearch(nums, target, middle + 1, high);
    }
}
```
三个关键点
- 计算middle下标时，不能简单用(low + high)/2，这样可能会导致溢出
- 取左半边和右半边区间时，左半边是[low, middle - 1]，右半边是[middle + 1, high]，这是两个闭区间。我们确定了middle点不是我们要找的，因此没有必要再把它加入到左右半边了。
- 对于一个长度为奇数的数组，按照low + (high - low) / 2 来计算的话，middle就是正中间那个位置，对于一个长度为偶数的数组，middle就是正中间靠左边的一个位置。

- 非递归
```java
int binarySearch(int[] nums, int target, int low, int high){
    while(low <= high){
        int middle = low + (high - low) /2;

        if(nums[middle] == target){
            return middle;
        }
        if(target < nums[high]){
            high = middle - 1;
        } else {
            low = middle + 1;
        }
    }
    return -1;
}
```

## 贪婪 Greedy
定义
贪婪是一种在每一步选择中都采用在当前状态下最好或最优的选择，从而希望导致结果是最好或最优的算法。

Leetcode #253

## 大厂高频真题
- 问题的剖析能力
- 寻找并分析解决问题的方案
- 代码的书写功底

Leetcode #3 无重复字符的最长子串
Leetcode #4 寻找两个有序数组的中位数 （#215 快速选择算法）
Leetcode #23 合并k个排列链表

Leetcode #56 合并区间
Leetcode #435 无重叠区间
Leetcode #269 火星字典
Leetcode #772 基本计算器III

## 大厂面试难题
Leetcode #10 正则表达式
Leetcode #84 柱状图最大的矩形
Leetcode #336 回文对
Leetcode #340 至多包含k个不同字符的最长字串
Leetcode #407 接雨水II


## Java常用数据结构和方法

数据结构
class | subclass
--- | ---
Set | AbstractSet, ConcurrentHashMap.KeySetView, ConcurrentSkipListSet, CopyOnWriteArraySet, EnumSet, HashSet, JobStateReasons, LinkedHashSet, TreeSet
List | AbstractList, AbstractSequentialList, ArrayList, AttributeList, CopyOnWriteArrayList, LinkedList, RoleList, RoleUnresolvedList, Stack, Vector
Map | AbstractMap, Attributes, AuthProvider, ConcurrentHashMap, ConcurrentSkipListMap, EnumMap, HashMap, HashTable, Headers, IdentityHashMap, LinkedHashMap, PrinterStateReasons, Properties, Provider, RenderingHints, ScriptObjectMirror, SimpleBindings, TabularDataSupport, TreeMap, UIDefaults, WeakHashMap
Queue | BlockingDeque, BlockingQueue, Deque, TransferQueue

方法
structure | insert | retieve | retrieve&remove | replace
--- | --- | --- | --- | ---
Set | add | - | remove | -
List | add | get | remove | set
LinkedList | add/offer | get/peek | remove/poll | set
Vector | add | get | remove | set
Stack | push | peek | pop | -
Map | put | get | remove | replace
Queue | add/offer | element/peek | remove/poll | -
Deque | addFirst/offerFirst</br>addLast/offerLast | getFirst/peekFirst</br>getLast/peekLast | removeFirst/pollFirst</br>removeLast/pollLast | -

## 刷题

按照力扣题目分类做题
树 > 图论 > 递归、回溯 > DFS、BFS > 动态规划 > 字符串和数组

https://github.com/jeantimex/javascript-problems-and-solutions

## Regular Expression

#### Formula
```
          { T[i-1][j-1] } if str[i] == pattern[j] || pattern[j] == '.'
          { T[i][j-2] || T[i-1][j] if (str[i] == pattern[j-1] || pattern[j-1] == '.') } if pattern[j] == '*'
T[i][j] = True if i<0 && j<0
          False otherwise

```

#### Sample
```
"ab"
".*"
true

"aab"
"c*a*b"
true

"ab"
".*c"
false

"aab"
"c*a*b"
true

"aaa"
"a*a"
true

"aaa"
"ab*a*c*a"
true

"mississippi"
"mis*is*ip*."
true

"aasdfasdfasdfasdfas"
"aasdf.*asdf.*asdf.*asdf.*s"
true
```
