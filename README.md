# 数碳智能后端源码
## 摘要与简介
数碳智能（DigitalCarbon）是基于Springboot、SpringSecurity、Quartz、Mybatis-plus、Redis、RabbitMQ、Vue、Django等框架架构的一套外贸企业碳排放管理数字化平台，支持多种形式的数据采样以及数据分析功能，并致力于为外贸企业出口碳排放控制做出决策。

## 后端部署环境
### 版本说明
- `^` 大于等于该指定的版本 `~` 小于等于该指定的版本
- `^~` 前置大版本号不超过也不低于指定的版本
- `=` 等于该指定的版本既不能大于该版本也不能小于该版本
- `|` 在指定的多个版本之间进行选择

### 环境版本控制
- `Maven: ^3.8.1`
- `Java: Oracle OpenJDK ^20 | Azul Zulu ^20`
- `Kotlin: =1.7`
- `Redis: ^~5.0.14.1`
- `MySQL: ^~8.0.28`
- `Erlang: ^~24.0 | ^~25.1`
- `RabbitMQ: ^~3.11.10`

### 部署数据库
使用 Navicat 或 MySQL WorkBench 连接并导入 digital_carbon 数据库，导入时选择 `utf8mb4` 字符集进行创建数据库操作，随后将 sql 文件进行导入操作。全部准备就绪后在 application.yml 中配置 datasource 下的数据库用户名与数据库密码（操作数据库和后端读取数据库数据时请避免**死锁**发生）。

### 部署Redis
通过 `redis-server [启动配置]` 来启动 Redis Server 服务，并通过 `redis-cli` 脚手架进行连接。必须在指定的启动配置文件中配置 Redis 的连接密码 `requirepass` 服务启动成功后再 redis-cli 中使用命令 `auth [requirepass]` 进行服务连接。全部准备就绪后在 application.yml 中配置 redis 的密码。

### 部署RabbitMQ
通过 `rabbitmq-server start` 来启动 rabbit-mq 服务，并通过 `rabbitmqctl status` 查询当前 rabbit-mq 服务的启动状态，对于高于 `25.0` 版本的 erlang 需要对调 `.erlang.cookie` 文件的位置才能启动 rabbitmq (安装完 mq 的插件后可以通过 web 进行访问，默认的 web 端口为 15672) 必须在配置文件中对 mq 的账号密码和端口进行配置，该配置文件中均使用本地环境中的默认配置。全部准备就绪后启动 xxx.jar 若未出现交换机绑定错误或队列创建错误即部署成功。若队列部署失败删除 mq 中的死信和延迟队列后重新启动。 

### 打包并发布至服务器
在打包前请先将 `application.yml` 文件中的 `spring.profiles.active` 改为 `online` 的线上部署环境配置！！！
使用 maven 的 `package Lifecycle` 将源码打包至 `\target\DigitalCarbon-backend-0.0.1-SNAPSHOT.jar` 将该 jar 包上传至服务器 `www\wwwroot\www.digital-carbon.org\` 下后启动即可。
部署完成后通过 `http://localhost:8080/doc.html` 来访问项目接口，在正式部署和项目上线前必须移除 `config.SecrutyConfig` 中 `"/doc.html"` 等接口的白名单，或自行添加 spring security 的登录账户。

## 开发指南
### 项目架构
DigitalCarbon backend 中 java 项目架构与功能模块划分如下所示， 此外 groovy 和 kotlin 只需按照 spring 项目外部工具、组件开发原则即可：

```text
config : 配置层
    用来存放RabbitMQ、Redis、Spring Security、Mybatis Plus、Swagger2等配置
controller : 控制层
    用于实现所有API接口逻辑与前置、后置、环绕切面逻辑
core : 核心层
    通常这里会写入一些核心数据结构，他们会在各个业务逻辑中被使用
    lazy : 延迟加载
        这是一个非常复杂的类的包装，不推荐在业务中使用LazyOptional，他只是对
        Java原版的Optional进行了一个并发环境的扩展
entity : 数据实体层
    用于映射数据库中所有的字段实体，所有数据来自mapper层从数据库读取的字段
extension : 扩展层
    用于控制Spring应用的扩展逻辑，如：切面层、事务层、异常扩展、组件层、线程池等
    annotation : 注解库
        一般搭配aspect包下的切面一起使用，对service.impl中的类进行切面控制
    aspect : 切面逻辑层
        用于实现切面逻辑，默认使用AspectJ实现接口层的所有切面逻辑
        尽量使用注解的形式进行切面操作，不推荐使用EL表达式进行切面控制
    component : 组件层
        和util包中不同的是，组件层一般是和spring框架密切联系的Spring Bean类
        他们通常都是来自maven中扩展的spring功能组件
    exception : 异常扩展
        主要对该框架中各种地方可能出现的新的异常的类
    param : 参数层
        通常是与controller层中需要传递的参数体进行映射，他们通常被注解
        @RequestBody修饰，并与单个参数@RequestParam进行区分 
mapper : 数据交互层
    用来映射resource/mapper下的所有mapper操作配置，以interface的形式进行一一映射
service : 业务逻辑层
    用来定义所有业务逻辑操作，如：用户账户管理业务、数据控制业务等
    该包中除了impl子包若无特殊要求一律定义为interface类型
    impl : 实现层
        用来实现service包中所有interface接口类，通常是接口对应的业务逻辑
util : 工具层
    用来定义常用工具类，如：文件IO、数据加密规则、RSA公钥生成、数据格式校验等
    该包中所有类若无特殊要求一律定义为static类型
```
