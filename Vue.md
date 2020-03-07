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

## slots vs props
When you need to pass HTML or other markup to a component, slots are way to go;
When you need to pass data to a component, props are perfect.

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

**v-slot**
内容插槽，简写成`#`(since Vue 2.6.0)
class binding on the slot element won't work because when the component is rendered, the slot element is replaced with the component content we pass in. To make it work we can wrap the slot inside another element.
We can pass multiple HTML elements of any sort into slot. We can also use use multiple slots in a component and put them in different place. This is where named slots come into play.
Whetever content we put into the component element will be rendered in the default slot. In order to send content to named slot, we can use the `v-slot` directive on a template element providing the name of a slot as v-slot's argument
*index.html*
*app.js*
```javascript
Vue.component('page-layout', {
    template: '#page-layout-template'
})

new Vue({
  el: "#app"
})
```
```xml
<body>
    <div id="app">
      <page-layout>
        <template #header>
          <h1>Here might be a page title</h1>
        </template>

        <p>A paragraph for the main content.</p>
        <p>And another one.</p>

        <template #footer>
          <p>Here's some contact info</p>
        </template>
      </page-layout>
    </div>


    <script type="text/x-template" id="page-layout-template">
      <div class="container">
        <header>
          <slot name="header"></slot>
        </header>
        <main>
          <slot></slot>
        </main>
        <footer>
          <slot name="footer"></slot>
        </footer>
      </div>
    </script>

    <script src="https://unpkg.com/vue"></script>
    <script src="app.js"></script>
    <style>
        .container {
          max-width: 1920;
          padding: 10px;
        }

        header {
          width: 100%;
          margin-bottom: 20px;
          text-align: center;
        }

        footer {
          width: 100%;
          margin-top: 50px;
          text-align: center;
        }
    </style>
</body>
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
![Vue Lifecycle](https://vuejs.org/images/lifecycle.png)
Example: created hook, which is a function just like the data and has access to the component's instance.
```javascript
let BlogPostComponent = {
    props: ['id'],
    data() {
        return {
            blogPost: null
        }
    },
    created () {
        axios.get('api/posts/' + this.id).then(response => {
            this.blogPost = response.data
        })
    }
}
```

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

## 组件
**Component's Template**
*app.js*
```javascript
Vue.component('click-counter', {
    template: '<button @click="count++">{{count}}</button>',
    data () {
        return {
            count: 0
        }
    }
})
new Vue({
    el: '#app'
})
```
*index.html*
```xml
<body>
    <div id="app">
        <click-counter></click-counter>
        <click-counter></click-counter>
    </div>
</body>
```
Note:
- template option must be a single root element.(so just wrap everything in a root div)
- data option is a function that returns an object rather than an object as it is in regular Vue instances so that each instances of the components can maintain an independent copy of the returned data object.

**x-template**
*app.js*
```javascript
Vue.component('click-counter', {
    template: '#click-counter-template',
    data () {
        return {
            count: 0
        }
    }
})
new Vue({
    el: '#app'
})
```
*index.html*
```xml
<script type="text/x-template" id="click-counter-template">
    <button @click="count++">{{count}}</button>
</script>
```

**Reusable Components with Props**
Two Methods:
- Register custom attributes on a component.
*app.js*
```javascript
Vue.component('plan', {
    template: '#plan-template',
    props: ['name']
})

// 或者在props中定义变量类型
Vue.component('plan', {
    template: '#plan-template',
    props: {
        name: String
    }
})

// 或者在props中定义变量类型和默认值
Vue.component('plan', {
    template: '#plan-template',
    props: {
        name: {
            type: String,
            default: 'Alex',
            required: true
        }
    }
})

new Vue({
    el: '#app'
})
```
*index.html*
```xml
<body>
    <div class="plans">
        <plan name='Learning Vue'></plan>
        <plan name='Learning React'></plan>
        <plan name='Learning Angular'></plan>
    </div>

    <script type="text/x-template" id='plan-template'>
        <div class="plan">
            <div class="description">
                <span class="title">
                    {{name}}
                </span>
            </div>
        </div>
    </script>
</body>
```
- Create an array with the plan names in the main vue instance and pass them dynamically, instead of hardcoding the plan names in the markup
*app.js*
```javascript
Vue.component('plan', {
    template: '#plan-template',
    props: ['name']
})
new Vue({
    el: '#app',
    data: {
        plans: ['Learning Vue', 'Learning React', 'Learning Angular']
    }
})
```
*index.html*
```xml
<body>
    <div class="plans">
        <plan v-for"plan in plans" :name='plan'></plan>
    </div>

    <script type="text/x-template" id='plan-template'>
        <div class="plan">
            <div class="description">
                <span class="title">
                    {{name}}
                </span>
            </div>
        </div>
    </script>
</body>
```

**Scope**
- Global Component:  makes the components globalling available in our application
`Vue.component()`
- Local Component: define the component as a javascript object and register it where we need to use it
*app.js*
```javascript
let PlanComponent = {
    template: '#plan-template',
    props: {
        name: {
            type: String,
            default: 'Alex',
            required: true
        }
    }
}

let PlanPickerComponent = {
    template: '#plan-picker-template',
    components: {
        plan: PlanComponent
    }
    data() {
        return {
            plans:['Learning Vue', 'Learning React', 'Learning Angular']
        }
    }
}

new Vue({
    el: '#app',
    components: {
        'plan-picker': PlanPickerComponent
    }
})
```
*index.html*
```xml
<script type="text/x-template" id="plan-template">
    <div class="plan">
        <div class="description">
            <span class="title">
                {{name}}
            </span>
        </div>
    </div>
</script>

<script type="text/x-template" id="plan-picker-template">
    <div class="plans">
        <plan v-for="plan in plans" :name="plan"></plan>
    </div>
</script>
```

**Communication Between Components with Custom Events**
We know that we can pass data from parent to child components using props. How to pass data from child to parent?
We solve this by using custom events. To send a custom event, we use the special `$emit` method.
- The first argument is the name of the event we want to emit.
- (optional) The second argument is the data we want to pass along with the event. The event data is always called payload.
Then we can listen for the event in the parent component using `v-on` or `@`
*app.js*
```javascript
let PlanPickerItemComponent = {
    template: '#plan-picker-item-template',
    props: {
        name: {
            type: String,
            default: 'Alex',
            required: true
        },
        selectedPlan: {
            type: String,

        }
    },
    computed: {
        isSelected(){
            return this.selectedPlan === this.name
        }
    }
    methods: {
        select() {
            this.$emit('select', this.name)
        }
    }
}

let PlanPickerComponent = {
    template: '#plan-picker-template',
    components: {
        'plan-picker-item': PlanPickerItemComponent // wrap component name in quotes in order to support the hyphen
    }
    data() {
        return {
            plans:['Learning Vue', 'Learning React', 'Learning Angular'],
            selectedPlan: null
        }
    },
    methods: {
        selectPlan(plan) {
            this.selectPlan = plan
        }
    }
}

new Vue({
    el: '#app',
    components: {
        'plan-picker': PlanPickerComponent
    }
})
```
*index.html*
```xml
<body>
    <div>
        <plan-picker></plan-picker>
    </div>

    <script type="text/x-template" id="plan-picker-item-template">
        <div @click="select" class="plan" :class="{'active-plan': isSelected}">
            <div class="description">
                <span class="title">
                    {{name}}
                </span>
            </div>
        </div>
    </script>

    <script type="text/x-template" id="plan-picker-template">
        <div class="plans">
            <plan-picker-item v-for="plan in plans" :name="plan" @select="selectPlan" :selected-plan="selectedPlan"></plan>
        </div>
    </script>
</body>
```
* \* Notes: HTML attribute names are case-insensitive, so browsers will interpret any uppercase characters as lowercase. That means when you’re using in-DOM templates, camelCased prop names need to use their kebab-cased (hyphen-delimited) equivalents *
```javascript
Vue.component('blog-post', {
  // camelCase in JavaScript
  props: ['postTitle'],
  template: '<h3>{{ postTitle }}</h3>'
})
```
```xml
<!-- kebab-case in HTML -->
<blog-post post-title="hello!"></blog-post>
```
