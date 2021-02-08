thrift
---

thrift 使用示例工程

### 1. 环境准备

#### 1.1 thrift编译器准备

**MAC操作系统**

直接使用homebrew进行安装

```bash
brew install boost openssl libevent bison
brew install thrift
```

安装完毕之后，执行以下命令测试一下

```bash
which thrift

thrift --version
```

**WIN操作系统**

直接下载可执行的exe即可

- [https://thrift.apache.org/download](https://thrift.apache.org/download)

选中`Thrift compiler for Windows (thrift-0.13.0.exe)`下载即可;

```bash
# 打开 powershell

# 下载
wget http://www.apache.org/dyn/closer.cgi?path=/thrift/0.13.0/thrift-0.13.0.exe
# 重命名
mv thrift-0.13.0.exe thrift.exe
```

接下来，进行环境变量配置(windows常规的环境变量配置流程)

- 电脑右键 -> 属性 -> 高级系统设置 -> 环境变量 -> 系统环境变量 -> 新建 

```
变量名: thrift
变量值: 下载的thrift目录，如 D:\workspace\tools\thrift
```

- 接下来选中PATH变量，双击 -> 新建 -> 添加 `%thrift%` -> 确定

最后新开一个powshell终端，测试一下

```bash
thrift --version
```


#### 1.2 idea环境配置

> 暂时没有完成

首先安装插件: `thrift support`, 安装完毕之后重启

项目配置

- project settings -> facets -> add -> Thrift
- 在右边弹出的编辑框中，新增 `Target = Java, output path=输出Java文件路径`  
- 将thrift文件放在 `src/main/java` 代码目录下
- 右键 -> `Recompile xxx.thrift` (实测，这一步执行完毕之后并没有生成java文件)
 
