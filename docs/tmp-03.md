# mybatis


动态sql的产生就来自与如下方法
```text
org.apache.ibatis.scripting.xmltags.DynamicSqlSource.getBoundSql

public BoundSql getBoundSql(Object parameterObject) {
    DynamicContext context = new DynamicContext(configuration, parameterObject);
    // 不同的
    rootSqlNode.apply(context);
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    context.getBindings().forEach(boundSql::setAdditionalParameter);
    return boundSql;
}
```

## SqlNode

复杂的xml配置,会生成MixedSqlNode,MixedSqlNode是个复合体,其包含不同的SqlNode.

SqlNode的实现类有:
```text
VarDeclSqlNode
ForEachSqlNode
IfSqlNode
ChooseSqlNode
TrimSqlNode
StaticTextSqlNode
```

SqlNode的主要方法的用途
```text
public class StaticTextSqlNode implements SqlNode {
  private final String text;

  public StaticTextSqlNode(String text) {
    this.text = text;
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 一段一段的append sql片段
    context.appendSql(text);
    return true;
  }

}
```