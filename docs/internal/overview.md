
```text
一句话概括 MyBatis 原理

MyBatis 通过解析配置文件，建立 Java 对象与数据库 SQL 的映射关系，底层封装 JDBC，自动完成参数设置、结果集封装，让开发者只关注业务 SQL。
```

```text
MyBatis 核心执行流程



1. 加载配置（初始化）
读取两大配置文件：
    mybatis-config.xml（全局配置：数据源、事务、环境等）
    Mapper.xml/ 注解（SQL 语句、结果映射规则）
MyBatis 把这些配置解析成Configuration对象（全局配置中心）。

2. 创建 SqlSessionFactory
用 Configuration 构建 SqlSessionFactory（会话工厂，单例）。
作用：生产 SqlSession。

3. 创建 SqlSession
SqlSession 是核心操作对象，对应一次数据库连接。
作用：发送 SQL、执行事务、获取 Mapper 接口。

4. 执行 SQL（核心）
通过 Mapper 接口 找到对应的 SQL（通过方法名绑定）。
MyBatis 底层用 Executor（执行器） 调用 JDBC。
自动做两件事：
    参数映射：Java 对象 → SQL 占位符参数
    结果映射：数据库结果集 → Java 对象

5. 释放资源
关闭 SqlSession，释放连接。
```

```text
核心组件



组件	                        作用
Configuration	            存放所有配置信息，全局唯一
SqlSessionFactory	        生产 SqlSession 的工厂
SqlSession	                操作数据库的核心 API，线程不安全
Mapper 接口	                定义要执行的 SQL 方法（MyBatis 自动生成实现类）
Executor	                真正执行 SQL 的底层执行器（封装 JDBC）
MappedStatement	            封装一条 SQL、参数、结果映射规则
TypeHandler	                Java 类型 ↔ 数据库类型 转换
```


```text
调用链路



整体前置
Mapper 接口是JDK 动态代理，代理类是 MapperProxy
所有方法调用，都会进到 MapperProxy#invoke()
最终全部走 SqlSession 体系执行

举例: userMapper.selectById(1L);

一、第一步：Mapper 方法调用入口
进入代理类：MapperProxy#invoke(Object proxy, Method method, Object[] args)
判断是不是 Object 原生方法（toString/equals…），是就直接放行
否则走 MapperMethod#execute


二、第二步：MapperMethod 执行
核心方法：
MapperMethod.execute(SqlSession session, Object[] args)
内部做两件事：
解析当前方法是 增/删/改/查
拿到 MappedStatement（封装当前方法对应的 SQL、入参、返回值、映射规则）
调用 SqlSession 对应方法


三、第三步：SqlSession 层（默认实现 DefaultSqlSession）
核心方法：
DefaultSqlSession#selectOne(String statement, Object parameter)
内部实际调用：
DefaultSqlSession#selectList(...)
注意：MyBatis 底层不分查询单条 / 多条，selectOne 本质就是取 selectList 结果的第一条


四、第四步：进入执行器 Executor（核心）
Executor#query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
Executor 常见实现：
    SimpleExecutor（默认）
    ReuseExecutor
    BatchExecutor
    带缓存：CachingExecutor（二级缓存）

关键分支：
    先走一级缓存判断（本地缓存，SqlSession 级别）
    缓存没命中 → 走数据库查询
    调用：BaseExecutor#queryFromDatabase(...)


五、第五步：SQL 解析 & 参数处理
MappedStatement#getBoundSql(Object parameterObject)
    解析 XML / 注解 SQL
    处理 ${}、#{}
    SqlSource 解析 → 生成最终可执行的 PreparedSQL
    封装 BoundSql：最终 SQL、参数映射、入参
ParameterHandler#setParameters(PreparedStatement ps)
核心：
    #{} 会走 PreparedStatement 预编译、防 SQL 注入
    类型处理器 TypeHandler 完成 Java 类型 → 数据库类型赋值
    
    
六、第六步：JDBC 真正执行 SQL
StatementHandler#query(Statement statement, ResultHandler resultHandler)
默认实现：PreparedStatementHandler
底层原生 JDBC：preparedStatement.execute();


七、第七步：结果集封装（ORM 核心）
ResultSetHandler#handleResultSets(PreparedStatement ps)
核心逻辑：
    拿到 JDBC ResultSet 结果集
    根据 ResultMap / 实体属性映射
    反射 + TypeHandler
    数据库字段 → Java 实体对象 自动封装
    
    
    
八、完整精简链路
Mapper代理方法.invoke()
→ MapperMethod.execute()
→ DefaultSqlSession.selectOne/selectList()
→ Executor.query()
    一级缓存判断
→ BaseExecutor.queryFromDatabase()
→ MappedStatement.getBoundSql() 【解析SQL、处理#{}】
→ ParameterHandler.setParameters() 【参数赋值】
→ StatementHandler.query() 【JDBC执行SQL】
→ ResultSetHandler.handleResultSets() 【结果集封装为Java对象】    
```

```text
MyBatis 一级缓存是「单实例、单 SqlSession」本地缓存，多服务实例集群下，只靠一级缓存，必然数据不一致，完全无法保证一致性。



一、先回顾：一级缓存本质
一级缓存：SqlSession 级别本地缓存，默认永久开启、关不掉
存储位置：当前服务JVM 内存，只在当前 SqlSession 内有效
生命周期：
    同一次会话内多次查同一条 SQL → 走缓存，不查库
    commit / rollback / close、执行增删改 → 清空一级缓存
隔离范围：
    同一个服务实例的同一个请求 = 同一个 SqlSession
    不同服务实例、不同请求、不同 SqlSession，缓存完全隔离、互不感知



二、多服务实例场景 为什么不一致？
核心原因三点
    一级缓存是单机本地内存缓存，不同服务实例缓存互不共享；
    增删改只会清空当前实例当前 SqlSession 的一级缓存，无法通知其他机器过期；
    没有统一缓存中间件，各实例缓存数据各自独立，数据更新无法广播。



三、怎么解决集群数据一致性？
方案 1：手动清空一级缓存（强制失效）
更新 / 删除业务写完后，手动清空当前 SqlSession 一级缓存
sqlSession.clearCache();

方案 2：关闭业务查询的一级缓存（常用兜底）
方式一：Mapper 查询标签设置 <select id="getUser" flushCache="true">
方式二：每次查询手动新开独立 SqlSession
方案 3：利用事务特性（有限缓解）
    写操作提交后，当前 SqlSession 自动清空一级缓存

四、正经最终方案（生产必须这么做）
放弃靠一级缓存做集群共享缓存，正确架构：
关掉 MyBatis 二级缓存（默认不推荐、坑多、事务隔离差）
引入分布式缓存中间件：Redis
统一缓存方案：
    查：先查 Redis → 没命中查库 → 写入 Redis
    改 / 删：更新数据库 + 同步删除 / 更新 Redis 缓存
做到：多实例缓存统一受控，全局数据一致    
```