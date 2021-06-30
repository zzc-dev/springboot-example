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

# 二、run流程

https://www.cnblogs.com/theRhyme/p/how-does-springboot-start.html

## 2.1 源码

### 2.1.1 创建SpringApplication

```java
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "PrimarySources must not be null");
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
		this.webApplicationType = WebApplicationType.deduceFromClasspath();
		// 从类路径META-INF/spring.factroies获取所有的ApplicationContextInitializer保存
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
		// 从类路径META-INF/spring.factroies获取所有的ApplicationListener保存
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
		this.mainApplicationClass = deduceMainApplicationClass();
}
```

### 2.1.2 run

```java
public ConfigurableApplicationContext run(String... args) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		// IOC 容器
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
		configureHeadlessProperty();
		// 从类路径下META-INF/spring.factroies获取SpringApplicationRunListener
		SpringApplicationRunListeners listeners = getRunListeners(args);
		// 回调所有的SpringApplicationRunListener的starting()
		listeners.starting();
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
			//创建准备环境
			ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
			configureIgnoreBeanInfo(environment);
			Banner printedBanner = printBanner(environment);
			//创建webIOC还是普通IOC
			context = createApplicationContext();
			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
			//创建IOC
			prepareContext(context, environment, listeners, applicationArguments, printedBanner);
			//加载IOC容器中的所有组件
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
			// 回调所有的SpringApplicationRunListener的started()
			listeners.started(context);
			// 先调用IOC容器中ApplicationRunner.run
			// 再调用IOC容器中CommandLineRunner.run
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}

		try {
		    // 回调所有的SpringApplicationRunListener的running()
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
		return context;
}
```

#### 2.1.2.1 prepareEnvironment

```java
private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments) {
		// 1.Create and configure the environment 创建环境
		ConfigurableEnvironment environment = getOrCreateEnvironment();
		configureEnvironment(environment, applicationArguments.getSourceArgs());
		ConfigurationPropertySources.attach(environment);
		// 2.回调所有的SpringApplicationRunListener的environmentPrepared()表示环境准备完成
		listeners.environmentPrepared(environment);
		bindToSpringApplication(environment);
		if (!this.isCustomEnvironment) {
			environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
					deduceEnvironmentClass());
		}
		ConfigurationPropertySources.attach(environment);
		return environment;
}
```

#### 2.1.2.2 prepareContext

```java
private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
			SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
		context.setEnvironment(environment);
		postProcessApplicationContext(context);
		// Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
		// 调用（一）中保存的ApplicationContextInitializer的initialize(context),对context初始化
		applyInitializers(context);
		// 回调所有的SpringApplicationRunListener的contextPrepared()
		listeners.contextPrepared(context);
		if (this.logStartupInfo) {
			logStartupInfo(context.getParent() == null);
			logStartupProfileInfo(context);
		}
		// Add boot specific singleton beans
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		if (printedBanner != null) {
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
		if (beanFactory instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) beanFactory)
					.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		if (this.lazyInitialization) {
			context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
		}
		// Load the sources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[0]));
		// 回调所有的SpringApplicationRunListener的contextLoaded()
		listeners.contextLoaded(context);
}
```

#### 2.1.2.3 callRunners

```java
private void callRunners(ApplicationContext context, ApplicationArguments args) {
		List<Object> runners = new ArrayList<>();
		runners.addAll(context.getBeansOfType(ApplicationRunner.class).values());
		runners.addAll(context.getBeansOfType(CommandLineRunner.class).values());
		AnnotationAwareOrderComparator.sort(runners);
		for (Object runner : new LinkedHashSet<>(runners)) {
			if (runner instanceof ApplicationRunner) {
				callRunner((ApplicationRunner) runner, args);
			}
			if (runner instanceof CommandLineRunner) {
				callRunner((CommandLineRunner) runner, args);
			}
		}
}
```

## 2.2 总结

1. 实例化SpringApplication对象并初始化一些属性，如从类路径META-INF/spring.factroies获取所有的ApplicationListener保存
2. 创建一个StopWatch实例，用来记录SpringBoot的启动时间
3. 加载 `SpringApplicationRunListener` ，唯一实现`EventPublishingRunListener`
4. 发布SpringBoot开始启动事件`EventPublishingRunListener.starting()`
5. 创建和配置 `environment`，发布环境准备好的事件 `EventPublishingRunListener.environmentPrepared()`
6. 根据环境创建对应的ApplicationContext：Web类型，Reactive类型，普通的类型(非Web)
7. 初始化ApplicationContext：Web容器，发布容器准备和容器加载事件 `EventPublishingRunListener.contextPrepared()`、`EventPublishingRunListener.contextLoaded()`
8. 创建IOC容器完成bean的加载注册以及初始化，
9. stopWatch停止计时，日志打印总共启动的时间
10. 发布SpringBoot启动完成事件`EventPublishingRunListener.started()`
11. 调用ApplicationRunner和CommandLineRunner
12. 最后发布就绪事件`EventPublishingRunListener.runing()`，标志着SpringBoot可以处理就收的请求了







