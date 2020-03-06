# Vue

## 构建项目
1. 构建
```
vue create vue-admin
```
2. 启动
```
cd vue-admin
npm run serve
```

3. 入口
public/index.html -> main.js把加载的模块塞到index.html的app中

## 文件规则
**固定的3块内容**
template, script, style

**template**
必须有一层父元素，否则会报错。一般是div标签

**script**
```xml
<script>
    export default {
        name: "home",
        components: {},
        data(){
            return {}
        },
        created(){},
        mounted(){},
        methods: {},
        props: {},
        watch: {}
    }
</script>
```
name: 当前的名称
components: 组件，有引入组件时，放置组建名称
data: 数据，v-model绑定数据使用
created: 创建完成时（生命周期其中一个）
mounted: 挂载完成时（生命周期其中一个）
methods: 定义函数
props / watch: 子组件接受父组件参数

**style**
lang="scss": 定义类型
scoped：定义只有当前文件应用该样式，否则视为全局样式

## Vue指令
**v-model**
在表单控件或者组件上创建双向绑定。
input, select, textarea, component

**v-for**
基于源数据多次渲染元素或模板块
基础模式:
```xml
<div v-for="item in items" :key="item.id">
    {{ item.text }}
</div>
```
带索引：
```xml
<div v-for="(item, index) in items" :key="item.id">
    {{ item.text }}
</div>
```
注：必须要有唯一的key
当和v-if一起使用时，v-for的优先级比v-if更高。
但是，不建议在同一标签上使用，避免产生不友好现象。

**v-show**
通过display属性，控制隐藏dom元素

**v-if & v-else**
```xml
<button v-if="state === 'default'" class="btn btn-primary">Add Item</button>
<button v-else class="btn btn-cancel">Cancel</button>
```

**v-bind**
绑定属性，简写成`:`
动态绑定class有两种句法
- Object syntax: the object property is the name of the class we'd like to toggle, and its value is the conditional we respond to
```xml
<li v-for="item in items" :class="{strikeout: item.purchased}">{{item.label}}</li>
```
- Array syntax: more verbose but offer us more flexibility for toggling between different classes, like tracking multiple conditionals
```xml
<li v-for="item in items" :class="[item.purchased ? 'strikeout' : '', item.highPriority? 'priority' : '']"></li>
```

**v-on**
触发事件，简写成`@`
`@click=""` 点击事件
`@keyup.enter=""` 回车事件

## computed vs methods
When you need to change data, you will use methods;
When you need to cahnge the presentation of existing data, you'll use computed properties.


## Vue3.0新特性语法
**setup函数**
新的组件选项，用于在组件中使用Composition API的入口，主要放data，生命周期和自定义函数等。
```javascript
export default {
    setup(props, context){
    context.attrs
    context.slots
    context.parent
    context.root
    context.emit
    context.refs
    ...
    }
}
```

**reactive（声明单一对象时使用）**
取得一个对象并返回原始对象的响应数据处理。
```javascript
const obj = reactive({count:0})
```
**ref（声明基础数据类型变量时使用）**
内部值并返回一个响应性且可变的ref对象，ref对象具有.value方法指向内部值的单个属性
```
const number = ref(0)
```
获取值方式： number.value

**isRef 和 toRefs**
isRef:检查一个对象是否是ref对象
toRefs:将对象解构成基础数据组
```javascript
const unwrapped = isRef(foo)? foo.value: foo;

-------
function useMousePosition(){
    const pos = reactive({
        x:0,
        y:0
    });
    return toRefs(pos);
}
const {x, y} = useMousePosition();
```
toRefs将reactive对象转换成普通对象，保证对象解构或拓展运算符不会丢失原有响应式对象的响应。

**生命周期**
2.0生命周期和Composition API之间的映射：
```
beforeCreate -> 使用setup()
created -> 使用setup()
beforeMount -> onBeforeMount
mounted -> onMounted
methods -> 去除，普通方式写方法
beforeUpdate -> onBeforeUpdate
updated -> onUpdated
beforeDestroy -> onBeforeUnmount
destroyed -> onUnmounted
errorCaptured -> onErrorCaptured
```

除了2.x生命周期外，Composition API还提供了以下调试挂钩:
```
onRenderTracked
onRenderTriggered
```
两个钩子都收到DebuggerEvent类似于onTract和onTrigger

## 括号
symbol | limitation
--- | ---
`{{}}` | only evaluate one expression at a time

