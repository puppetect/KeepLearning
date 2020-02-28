# Proxy

## 静态代理
静态代理在使用时需要定义接口或者父类。

- 继承
代理对象继承目标对象，重写目标对象的方法。
目标对象:
```java
public class UserDaoImpl {
	public void login(){
		System.out.println("login success...");
	}
}
```
代理对象：
```java
public class UserLogProxy extends UserDaoImpl{
	@Override
	public void login(){
		System.out.println("---log---");
		super.login();
	}
}
```
缺点：
1.java是单继承，难以实现扩展
2.如果每个类都需要代理，代理类会爆炸

2.聚合
目标对象和代理对象实现同一个接口，并且代理对象中包含抽象对象。
抽象对象：
```java
public interface UserDao {
	void login()
}
```
目标对象：
```java
public class UserDaoImpl implements UserDao{
	@Override
	public void login(){
		System.out.println("login success...");
	}
}
```
代理对象：
```java
public class UserLogImpl implements UserDao{
	private UserDao target;
	public UserLogImpl(UserDao target){
		this.target = target;
	}
	@Override
	public void login(){
		System.out.println("---login---");
		target.login();
	}
}
```
缺点：类爆炸

## 动态代理
JDK动态代理
1. 实现InvocationHandler接口来定义自己的InvocationHandler
```java
public class MyHandler implements InvocationHandler {
	private Object object;
	public MyHandler(Object object){
		this.object = object;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 前置处理
		System.out.println("before log");
		Object invoke = method.invoke(object, args);
		// 后置处理
		System.out.println("after log");
		return invoke;
	}
}
```
2.


 