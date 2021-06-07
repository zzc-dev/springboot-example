# 一、Springboot 自动配置原理

https://blog.csdn.net/u014745069/article/details/83820511

## 1.1 SpringBootApplication

首先看Springboot的启动类，该启动类上有个注解`@SpringBootApplication`

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {}

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface SpringBootConfiguration {
}
```

`SpringBootApplication` 是一个复合注解，他上面有一个`SpringBootConfiguration`，该注解的功能十分简单，就是标记该类是一个配置类。

另一个注解是我们的重点：`EnableAutoConfiguration` 顾名思义，他就是我们开启自动配置的注解。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({EnableAutoConfigurationImportSelector.class})
public @interface EnableAutoConfiguration {
    String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

    Class<?>[] exclude() default {};

    String[] excludeName() default {};
}
```

## 1.2 EnableAutoConfiguration

​	关键功能由@Import提供，在SpringIOC中我们知道，spring在`invokeBeanFacotryPostProcessors()`时，会去加载注册bean，包括被Import引入的bean，如果该bean是`ImportSelector` 的子类，该bean将会实例化，并且调用其`selectImports()` 向spring容器中引入额外的bean。

​	`selectImports()`的作用是扫描所有具有 **META-INF/spring.factories** 的jar包。

​	spring.factories文件也是一组一组的key=value的形式，其中一个key是EnableAutoConfiguration类的全类名，而它的value是一个xxxxAutoConfiguration的类名的列表，这些类名以逗号分隔，如下图所示。最终将这些自动配置类加载到Spring容器中。

```java
public String[] selectImports(AnnotationMetadata annotationMetadata) {
    if (!this.isEnabled(annotationMetadata)) {
        return NO_IMPORTS;
    } else {
        try {
            AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader); // 这里加载META-INF/spring-autoconfigure-metadata.properties
            AnnotationAttributes attributes = this.getAttributes(annotationMetadata);
            List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
            configurations = this.removeDuplicates(configurations);
            configurations = this.sort(configurations, autoConfigurationMetadata);
            Set<String> exclusions = this.getExclusions(annotationMetadata, attributes);
            this.checkExcludedClasses(configurations, exclusions);
            configurations.removeAll(exclusions);
            configurations = this.filter(configurations, autoConfigurationMetadata);
            this.fireAutoConfigurationImportEvents(configurations, exclusions);
            return (String[])configurations.toArray(new String[configurations.size()]);
        } catch (IOException var6) {
            throw new IllegalStateException(var6);
        }
    }
}

public static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader) {
        return loadMetadata(classLoader, "META-INF/spring-autoconfigure-metadata.properties");
    }
```

![image-20210607170240608](D:\myself\springboot-example\文档\typora\images\springboot01.png)

## 1.3 以EmbeddedWebServerFactoryCustomizerAutoConfiguration为例

以ServletWebServerFactoryAutoConfiguration配置类为例，解释一下全局配置文件中的属性如何生效

![img](D:\myself\springboot-example\文档\typora\images\springboot02.png)

在ServletWebServerFactoryAutoConfiguration类上，有一个`@EnableConfigurationProperties`注解：**开启配置属性**，而它后面的参数是一个ServerProperties类，这就是习惯优于配置的最终落地点。

![image-20210607171837487](D:\myself\springboot-example\文档\typora\images\springboot04.png)

![img](D:\myself\springboot-example\文档\typora\images\springboot03.png)



​	在这个类上，我们看到了一个非常熟悉的注解：@ConfigurationProperties，它的作用就是从配置文件中绑定属性到对应的bean上，而@EnableConfigurationProperties负责导入这个已经绑定了属性的bean到spring容器中（见上面截图）。那么所有其他的和这个类相关的属性都可以在全局配置文件中定义，也就是说，真正“限制”我们可以在全局配置文件中配置哪些属性的类就是这些XxxxProperties类，它与配置文件中定义的prefix关键字开头的一组属性是唯一对应的。

​	至此，我们大致可以了解。在全局配置的属性如：server.port等，通过@ConfigurationProperties注解，绑定到对应的XxxxProperties配置实体类上封装为一个bean，然后再通过@EnableConfigurationProperties注解导入到Spring容器中。

## 1.4 总结

​	Spring Boot启动的时候会通过@EnableAutoConfiguration注解找到META-INF/spring.factories配置文件中的所有自动配置类，并对其进行加载，而这些自动配置类都是以AutoConfiguration结尾来命名的，它实际上就是一个JavaConfig形式的Spring容器配置类，它能通过以Properties结尾命名的类中取得在全局配置文件中配置的属性如：server.port，而XxxxProperties类是通过@ConfigurationProperties注解与全局配置文件中对应的属性进行绑定的。













