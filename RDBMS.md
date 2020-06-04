# RDBMS

## Oracle SQL

### JDBC
```
jdbc:oracle:thin:@<host>:1521:<SID>
jdbc:oracle:thin:@//<host>:1521/<SID>
jdbc:oracle:thin:@//<host>:1521/<SERVICENAME>
```

### 新建数据库
[使用DBCA创建数据库]（https://www.jianshu.com/p/82a3cdb3f7fb）


### 用户

#### 类型

sys; // 系统管理员，拥有最高权限
system; // 本地管理员，次高权限
scott; // 普通用户，密码默认为tiger,默认未解锁

#### 登陆
```
sqlplus / as sysdba;    // 登陆sys帐户
sqlplus sys as sysdba;    // 同上
sqlplus scott/tiger;    // 登陆普通用户scott
```

#### 管理
```
create user zrunker;    // 在管理员帐户下，创建用户zrunker
alter user scott identified by tiger;    // 修改密码
```

#### 授予权限
1. 默认的普通用户scott默认未解锁，不能使用，新建的用户也没有任何权限，必须授予权限
```
grant create session to zrunker;    // 授予zrunker用户创建session的权限，即登陆权限
grant unlimited tablespace to zrunker;    // 授予zrunker用户使用表空间的权限
grant create table to zrunker;    // 授予创建表的权限
grante drop table to zrunker;    // 授予删除表的权限
grant insert table to zrunker;    // 插入表的权限
grant update table to zrunker;    // 修改表的权限
grant all to public;    // 这条比较重要，授予所有权限(all)给所有用户(public)
```
解锁scott
```
alter user scott account unlock;
alter user scott identified by tiger;
```
2. oralce对权限管理比较严谨，普通用户之间也是默认不能互相访问的，需要互相授权
```
grant select on tablename to zrunker;    // 授予zrunker用户查看指定表的权限
grant drop on tablename to zrunker;    // 授予删除表的权限
grant insert on tablename to zrunker;    // 授予插入的权限
grant update on tablename to zrunker;    // 授予修改表的权限
grant insert(id) on tablename to zrunker;
grant update(id) on tablename to zrunker;    // 授予对指定表特定字段的插入和修改权限，注意，只能是insert和update
grant alert all table to zrunker;    // 授予zrunker用户alert任意表的权限
```

#### 撤销权限
基本语法同grant,关键字为revoke

#### 查看权限
```
select * from user_sys_privs;    // 查看当前用户所有权限
select * from user_tab_privs;    // 查看所用用户对表的权限
```

#### 操作表的用户的表
```
select * from zrunker.tablename
```

#### 权限传递
即用户A将权限授予B，B可以将操作的权限再授予C，命令如下：
```
grant alert table on tablename to zrunker with admin option;    // 关键字 with admin option
grant alert table on tablename to zrunker with grant option;    // 关键字 with grant option效果和admin类似
```

#### 角色
角色即权限的集合，可以把一个角色授予给用户
```
create role myrole;    // 创建角色
grant create session to myrole;    // 将创建session的权限授予myrole
grant myrole to zrunker;    // 授予zrunker用户myrole的角色
drop role myrole;    // 删除角色
```

### 表

#### 删除所有表
脚本生成
```
SELECT 'DROP TABLE "' || TABLE_NAME || '" CASCADE CONSTRAINTS;' FROM user_tables;
```

## 索引

#### 定义
索引就是按用户任意指定的字段对数据进行排序的一种数据结构

#### b+树
1. 树的高度就是磁盘I/O的次数，所以b+树一个叶子节点能有多个数据的这种结构能提高效率
2. 叶子节点完整保留了用户数据，并且是双向链表结构，可以在>=之类的范围查询中提高效率

#### 主键索引、联合索引
主键索引的叶子节点会保存完整的用户数据，而辅助索引的叶子节点则只会保留主键，通过索引查询到主键后再利用主键索引去查询完整的数据，这个过程称为回表

#### 最左前缀匹配原则
若某表有复合索引（b,c,d），用前缀不完整的语句(只指定c和d，未指定b)去查询，则无法利用该复合索引。
反之不然，如果后缀不完整（只指定b，未指定c和d），也能利用复合索引，mysql会一直向右匹配直到遇到范围查询（>, <, between, like）


#### 升序索引和降序索引

#### 索引优化

