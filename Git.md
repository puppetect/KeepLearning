# Git

## 基础命令
拷贝项目
`git clone <repository>`
创建分支
`git branch <name>`
创建并进入分支
`git checkout -b <name>`
切分支
`git checkout <name>`
查看状态
`git status`
添加所有文件
`git add`
提交
`git commit -m <message>`
拉取
`git pull`
推送
`git push`
查看分支
`git branch --list`
查看分支（包含远程分支）
`git branch -a`
删除track的文件
`git rm <file>`

## 撤销操作
https://sethrobertson.github.io/GitFixUm/fixup.html#discard_all_unpushed

## 项目分支管理
master: 主分支，一般不会在此分支上开发项目
dev: 开发分支，一般在此分支上开发

版本分支：建立与dev分支下面
feature-vueAdmin-V1.0.0-20190919: 分支完整名称
feature: 描述当前分支类型（需求）
vueAdmin: 项目名称
V1.0.0: 版本号
20190919: 建立分支日期

BUG分支： 建立与当前版本分支下面
bug-101241-20191010: bug分支完整名称
bug: 分支类型(BUG)
101241: bug的ID
20190919: 建立分支日期

```
Repo
├── master
└── dev
    ├── feature-vueAdmin-V1.0.0-20190919
    │   ├── bug-101241-20191020
    │   └── bug-101242-20191020
    ├── feature-vueAdmin-V2.0.0-20190919
    │   └── bug-101243-20191020
    └── feature-vueAdmin-V3.0.0-20190919
```
## gitignore
```
HELP.md
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**
!**/src/test/**

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
build/

### VS Code ###
.vscode/
```
