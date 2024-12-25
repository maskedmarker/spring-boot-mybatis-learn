


# @EnableTransactionManagement
```text
@Import(TransactionManagementConfigurationSelector.class)
public @interface EnableTransactionManagement {}

public class TransactionManagementConfigurationSelector extends AdviceModeImportSelector<EnableTransactionManagement> {}


public abstract class AdviceModeImportSelector<A extends Annotation> implements ImportSelector {}
```

## TransactionManagementConfigurationSelector
TransactionManagementConfigurationSelector动态引入了
AutoProxyRegistrar/ProxyTransactionManagementConfiguration
AspectJJtaTransactionManagementConfiguration/AspectJTransactionManagementConfiguration

```java
public class TransactionManagementConfigurationSelector extends AdviceModeImportSelector<EnableTransactionManagement> {
    
	@Override
	protected String[] selectImports(AdviceMode adviceMode) {
		switch (adviceMode) {
			case PROXY:
				return new String[] {AutoProxyRegistrar.class.getName(), ProxyTransactionManagementConfiguration.class.getName()};
			case ASPECTJ:
				return new String[] {determineTransactionAspectClass()};
			default:
				return null;
		}
	}

    private String determineTransactionAspectClass() {
        return (ClassUtils.isPresent("javax.transaction.Transactional", getClass().getClassLoader()) ?
                TransactionManagementConfigUtils.JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME :
                TransactionManagementConfigUtils.TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME);
    }
}
```


## ImportSelector
以编程方式(而非声明方式),动态import需要被bean
```java
public interface ImportSelector {
    // Select and return the names of which class(es) should be imported based on the AnnotationMetadata of the importing @Configuration class
    // 返回bean的class name
    String[] selectImports(AnnotationMetadata importingClassMetadata);
}
```

## configuration-class
ConfigurationClassParser可以基于Class的metadata信息来导入更多的bean

```java
public class ConfigurationClassParser {
    
    protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass, Predicate<String> filter) throws IOException {
        // ...
    }
}
```