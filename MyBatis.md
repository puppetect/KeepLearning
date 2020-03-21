# MyBatis

## 两大难点
1. dao接口如何变成对象？
    jdk动态代理产生了代理对象
2. 如何执行sql？
    通过InvocationHandler接口的实现类的invoke方法得到注解里的sql再执行
3. 如何把产生的代理**对象**注入到容器中？（如何把第三方或自己产生的对象交给Spring管理）
    首先，把对象交给spring和把类传给Spring后让Spring去实例化是不同的，后者不能控制对象的产生过程
    其次，spring中自己实例化有三种方式：
    1. @Bean (但是需要挨个注解，不高效)
    2. spring API `beanFactory.registerSingleton(beanName, Object)` （缺点同上）
    3. FactoryBean （缺点同上）
```java
public class MapperFactoryBean implements FactoryBean, InvocationHandler {

    Class mapperInterface;

    public MapperFactoryBean(Class clazz) {
        this.mapperInterface = clazz;
    }

    //實現InvocationHandle接口重寫invoke方法，這里就是我們要調用bean對象的邏輯方法
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Select annotation = method.getAnnotation(Select.class);
        if(annotation != null) {
            String s = annotation.value()[0];
            // 通过s（sql）去获取对象并返回
        }
        return proxy;
    }

    /**
     * factoryBean接口特性再getBean()會調用此方法
     * 思路：
     * 1、我們充分利用factoryBean接口的特性再結合反射就會返回一個代理類充當bean添加到工廠
     * */
    @Override
    public Object getObject() throws Exception {
        Class [] classes = new Class[]{mapperInterface};
        // 动态代理
        Object proxy =  Proxy.newProxyInstance(this.getClass().getClassLoader(),classes,this);
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
```
    4. FactoryMethod
