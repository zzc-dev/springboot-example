[批次上传9SpringIOC源码解析(上)](https://mp.weixin.qq.com/s?__biz=MzU5MDgzOTYzMw==&mid=2247484561&idx=1&sn=a7281dae7aaaa3499d59dec730464e63&scene=21#wechat_redirect)

[SpringIOC源码解析(下)](https://mp.weixin.qq.com/s?__biz=MzU5MDgzOTYzMw==&mid=2247484564&idx=1&sn=84bd8fee210c0d00687c3094431482a7&scene=21#wechat_redirect)

# 一、IOC

## 1. xml

```java
public void refresh() throws BeansException, IllegalStateException {
   synchronized (this.startupShutdownMonitor) {
      // 准备工作，记录下容器的启动时间、标记“已启动”状态、处理配置文件中的占位符
      prepareRefresh();

      //TODO 负责BeanFactory的初始化，Bean的加载和注册
      //这步比较关键，这步完成后，配置文件就会解析成一个个 Bean 定义，注册到 BeanFactory(DefaultListableBeanFactory) 中，
      //当然，这里说的 Bean 还没有初始化，只是配置信息都提取出来了，
      //注册也只是将这些信息都保存到了注册中心(说到底核心是一个 beanName-> beanDefinition 的 map)
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

      // 1.设置BeanFactory的类加载器
      // 2.添加几个BeanPostProcessor
      // 3.手动注册几个特殊的bean
      prepareBeanFactory(beanFactory);

      try {
         // Spring扩展点，子类通过重写该方法在【预处理后自定义对BeanFactory做进一步的设置】
         postProcessBeanFactory(beanFactory);

         //TODO 找出所有beanFactory后置处理器，并且调用这些处理器来改变bean的定义
         invokeBeanFactoryPostProcessors(beanFactory);

         //TODO 向beanFoctory中注册并实例化BeanPostProcessor,现在并没有调用处理器中的方法改变bean
         registerBeanPostProcessors(beanFactory);

         // 针对国际化的，暂时不去了解
         initMessageSource();

         // 事件监听器 观察者模式运用， 一个有改变, 通知所有应用监听器做对应的处理
         initApplicationEventMulticaster();

         onRefresh();

         registerListeners();

         // 初始化所有的没有设置懒加载的singleton bean.
         finishBeanFactoryInitialization(beanFactory);

         finishRefresh();
      }

      catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
            logger.warn("Exception encountered during context initialization - " +
                  "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();

         // Reset 'active' flag.
         cancelRefresh(ex);

         // Propagate exception to caller.
         throw ex;
      }

      finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
      }
   }
}
```

## 2. annotation

```java
public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
   this();
   // 将annotatedClasses本身作为bean注册到BeanFactory中
   // 向spring容器中注册几个默认的后置处理器、配置类、监听器的处理器
   // org.springframework.context.annotation.internalConfigurationAnnotationProcessor ConfigurationClassPostProcessor
   // org.springframework.context.annotation.internalAutowiredAnnotationProcessor
   // org.springframework.context.annotation.internalCommonAnnotationProcessor
   // org.springframework.context.annotation.internalRequiredAnnotationProcessor

   // org.springframework.context.event.internalEventListenerFactory
   // org.springframework.context.event.internalEventListenerProcessor  【SmartInitializingSingleton】 
   register(annotatedClasses);
   //重点
   refresh();
}

public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// 准备工作，记录下容器的启动时间、标记“已启动”状态、处理配置文件中的占位符
			prepareRefresh();
            
			//TODO 基于注解这步与xml完全不同，只是为了获取当前的BeanFactory
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            // 填充beanFactory的一些属性
			// 1.设置BeanFactory的类加载器
			// 2.添加几个BeanPostProcessor
			// 3.手动注册几个特殊的bean
			prepareBeanFactory(beanFactory);

			try {
				// Spring扩展点，子类通过重写该方法在【预处理后自定义对BeanFactory做进一步的设置】
				postProcessBeanFactory(beanFactory);

				//TODO 将被注解修饰的bean注册到工厂中;
				//TODO 将实现ImportBeanDefinitionRegistrar的bean实例化并调用registerBeanDefinitions   该bean存在configClass.getImportBeanDefinitionRegistrars()
				//TODO 实例化@Import 下的Selector 和上面的Registrar一样，他们的目的都是将某个bean注册到spring容器中，因此，他们在parse时已经被实例化了，然后去调用方法注册bean
				//TODO 找出所有beanFactory后置处理器，并且调用这些处理器来改变bean的定义
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
				//TODO 向beanFoctory中注册并实例化BeanPostProcessor,现在并没有调用处理器中的方法改变bean
				registerBeanPostProcessors(beanFactory);

				// Initialize message source for this context.
				// 针对国际化的，暂时不去了解
				initMessageSource();

				// Initialize event multicaster for this context.
				// 事件监听器 观察者模式运用， 一个有改变, 通知所有应用监听器做对应的处理
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				onRefresh();

				// Check for listener beans and register them.
				registerListeners();

				// 初始化所有的没有设置懒加载的singleton bean.
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				finishRefresh();
			}

			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}

			finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				resetCommonCaches();
			}
		}
	}
```

## 3. 循环依赖

设置属性环节，需要互相依赖，形成闭环

构造器循环依赖：无法解决

属性setter依赖：<strong style="color:red">通过提前暴露对象的方式解决（三级缓存）</strong>

	1. 初始化和实例化分开
 	2. 设置缓存来预存对象（完成实例化但未初始化的对象）

### 3.1 三级缓存

```java
class DefaultSingletonBeanRegistry{
    //一级缓存 用于保存BeanName 和创建bean实例之间的关系
    Map<String, Object> singletonObjects;
    //二级缓存 保存BeanName和创建Bean实例之间的关系，与singletonFactories不同之处在于
    // 当一个单例bena放入之后，那么当bean还在创建过程中就可以通过getBean获取到，可以方便进行循环依赖的检测
    Map<String, Object> earlySingletonObjects; 
    //三级缓存 保存BeanName和创建Bean的工厂之间的关系
    Map<String,ObjectFactory<?>> singletonFactories;    
}
```

三级缓存的lambda

```java
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));

protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
   Object exposedObject = bean;
   if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
      for (BeanPostProcessor bp : getBeanPostProcessors()) {
         if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
            SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
            exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
         }
      }
   }
   return exposedObject;
}
```

### 3.2 问题

**Q1: 一二三级缓存中分别存放什么对象**

​	一级：成品对象（完成初始化的对象）

​	二级：半成品对象（完成实例化但尚未初始化的对象）

​	三级：lambda表达式，可以获取刚创建的对象，但也可能获取的是代理对象

**Q2: 如果只设置一级缓存能否解决循环依赖问题？**

​	不能。一级缓存无法区分成品和半成品对象，可能导致获取的对象还未进行初始化

**Q3: 如果只设置二级缓存能否解决循环依赖问题？**

​	不能。三级缓存中获取的对象可能是代理对象，导致bean版本不一致。

## 4. bean的生命周期

1. 加载注册配置类的bean

2. 创建BeanFactory并填充属性

3. 执行BeanFactoryPostProcessor的后置处理方法，有一个子类`ConfigurationClassPostProcessor` 将扫描配置类并将其他Bean加载注册到容器中

4. 向容器注册并初始化一些BeanPostProcessor

5. 创建并初始化单例非懒加载的bean
   1. 实例化bean
   2. 给bean的属性赋值 `populateBean`
   3. 执行aware接口
   4. 执行BeanPostProcessor的before方法
   5. 执行init-method()
   6. 执行BeanPostProcessor的after()
   
6. 如果 bean 实现DisposableBean 接口，当 spring 容器关闭时，会调用 destory()。

   如果为bean 指定了 destroy 方法（ <bean> 的 destroy-method 属性），那么将 调用它。

# 二、AOP

## 2.1 概念

面向切面编程，非侵入式。业务只需关注自身逻辑实现，而不用去做其他的事，如：日志、安全、事务等

- **通知Advice** 想要实现的功能
- **连接点JoinPoint** 允许使用通知的地方。方法前后、返回值后、抛异常后
- **切入点PointCut** 想要通知起作用的连接点，用来过滤连接点的
- **切面Aspect** 通知和切入点的结合
- **织入Weaving** 把切面应用到目标对象来创建新的代理对象的过程

## 2.2 @EnableAspectJAutoProxy的作用

```java
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {}


//向容器注册了org.springframework.aop.config.internalAutoProxyCreator  -> org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar

AnnotationAwareAspectJAutoProxyCreator -> InstantiationAwareBeanPostProcessor  -->BeanPostProcessor   
    
```

1. `invokeBeanFactoryPostProcessors` 执行BeanFactoryPostProcessor的postProcessor()时，将 `AspectJAutoProxyRegistrar` 该Bean注册加载到容器，由于它是ImportBeanDefinitionRegistrar的子类，此阶段会实例化并调用registrarBean向容器中注册 `AnnotationAwareAspectJAutoProxyCreator`
2. `registerBeanPostProcessor` 加载并实例化所有的后置处理器，其中包括了`AnnotationAwareAspectJAutoProxyCreator`
3. `finishBeanPostProcessor` 创建所有非懒加载的单例bean，在创建bean实例化并给属性赋值后，在初始化时，调用`AnnotationAwareAspectJAutoProxyCreator`的after()创建了代理对象



# 三、bean作用域

| 作用域      | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| singleton   | 在spring IoC容器仅存在一个Bean实例，Bean以单例方式存在，bean作用域范围的默认值。 |
| prototype   | 每次从容器中调用Bean时，都返回一个新的实例，即每次调用getBean()时，相当于执行newXxxBean()。 |
| request     | 每次HTTP请求都会创建一个新的Bean，该作用域仅适用于web的Spring WebApplicationContext环境。 |
| session     | 同一个HTTP Session共享一个Bean，不同Session使用不同的Bean。该作用域仅适用于web的Spring WebApplicationContext环境。 |
| application | 限定一个Bean的作用域为`ServletContext`的生命周期。该作用域仅适用于web的Spring WebApplicationContext环境。 |

# 四、事务

## 4.1 传播特性

​	Atomicity 原子性

​	Consistency 一致性

​	Isolation 隔离性

​	Durablity 持久性

## 4.2 配置方式

Spring支持编程式事务和声明式事务

编程式事务：侵入代码，代码块级别的事务

声明式事务：使用AOP，方式级别的事务，无侵入

## 4.3 传播机制

- **propagation_required**  默认，当前事务加入到外层事务，一块提交，一块回滚
- **propagation_requires_new** 每次都开启一个新事务，如果有外层事务，先挂起外层事务，当当前事务执行完后，恢复外层事务
- **propagation_support** 完全依赖外层事务。有则加入，无则以非事务方式执行
- **propagation_not_support** 不支持事务
- **propagation_never** 不支持外层事务，有外层事务就抛异常
- **propagation_mandatory** 只支持外层事务，如果没有外层事务，抛异常
- **propagation_nested** 可以保存状态点，当前事务回滚到某个点，从而避免所有的嵌套事务都回滚，即各自回滚各自的。

## 4.4 隔离级别

多个事务同时运行，操作同一数据，可能导致：

- **脏读** 读取了尚未提交的数据
- **不可重复读** 两次读取的数据不一致。另一个事务修改了该数据
- **幻读** 读取几行数据，两次读取发现数据增加或减少。另一个事务新增或删除了数据

| 隔离级别                  | 含义                                                     |
| ------------------------- | -------------------------------------------------------- |
| isolation_defalut         | 使用数据库默认的隔离级别                                 |
| isolation_read_uncommited | 允许读取未提交的数据。可能导致脏读、不可重复读、幻读     |
| isolation_read_commited   | 只允许读取已经提交的数据。可能导致不可重复读、幻读       |
| isolation_repeatale_read  | Mysql默认级别 对相同字段的多次读取是一致的。只会发生幻读 |
| isolation_serializable    | 完全服务ACID，性能最差，需要锁表完成                     |

## 4.5 只读

​	如果一个事务只对数据库执行读操作，那么该数据库就可能利用那个事务的只读特性，采取某些优化措施。通过把一个事务声明为只读，可以给后端数据库一个机会来应用那些它认为合适的优化措施。

​	由于只读的优化措施是在一个事务启动时由后端数据库实施的， 因此，只有对于那些具有可能启动一个新事务的传播行为（PROPAGATION_REQUIRES_NEW、PROPAGATION_REQUIRED、 ROPAGATION_NESTED）的方法来说，将事务声明为只读才有意义。

## 4.6 回滚规则

在默认设置下，事务只在出现运行时异常（runtime exception）时回滚，而在出现受检查异常（checked exception）时不回滚（这一行为和EJB中的回滚行为是一致的）。
不过，可以声明在出现特定受检查异常时像运行时异常一样回滚。同样，也可以声明一个事务在出现特定的异常时不回滚，即使特定的异常是运行时异常。

## 4.7 事务配置参考

```java
@Transactional(propagation=Propagation.REQUIRED)
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
@Transactional(readOnly=true)
@Transactional(timeout=30)
// 回滚指定单一异常类
@Transactional(rollbackFor=RuntimeException.class)
```

# Spring类

## 1. DefaultListableBeanFactory

### 1.1 属性

```java
Map<String, BeanDefinition> beanDefinitionMap;
List<String> beanDefinitionNames;
```

### 1.2 registerBeanDefinition 

```java
// 注册beanDefinition
public void registerBeanDefinition(String beanName, beanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
```

## 2. PostProcessorRegistrationDelegate

### 2.1 invokeBeanFactoryPostProcessors

 ```java
/**
	 * beanFactoryPostProcessors：
	 * 	spring自己的后置处理器
	 * 	AbstractApplicationContext.List<BeanFactoryPostProcessor> beanFactoryPostProcessors
	 * beanFactory
	 * 	.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false)
	 * 	.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false)
	 * 	获取用户实现了相应接口的bean
	 *
	 * 执行所有BeanFactory的postProcessBeanFactory(),具体细节
	 * 	1.找到beanFactoryPostProcessors 中为BeanDefinitionRegistryPostProcessor的所有子类
	 * 		1.1 执行 postProcessBeanDefinitionRegistry()
	 * 	2.找到beanFactory中实现了BeanDefinitionRegistryPostProcessor的bean
	 * 		2.1 执行实现PriorityOrdered的bean的postProcessBeanDefinitionRegistry()
	 * 		2.2 执行实现Order
	 * 		2.3	执行其他bean
	 * 		2.4 执行postProcessBeanFactory()
	 * 		2.5 执行【1】类中的postProcessBeanFactory()
	 *	3. 找到beanFactory中实现了BeanFactoryPostProcessor的bean
	 *		3.1 执行实现PriorityOrdered的bean的postProcessBeanFactory()
	 *		3.2 执行实现Order
	 *		3.3 执行其他bean
	 * */
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
 ```

### 2.2 registerBeanPostProcessors

```java
/**
 * 向BeanFactory注册并实例化BeanPostProcessor
 * 注册顺序：
 *   1.BeanPostProcessorChecker  --默认
 *   2.@PriorityOrdered
 *   3.@Ordered
 *   4.NoOrdered（没有上面两个注解的）
 *   5.MergedBeanDefinitionPostProcessor（它也是被@PriorityOrdered修饰的，所以会注册两次）
 *   6.ApplicationListenerDetector --默认
 * */
public static void registerBeanPostProcessors(
      ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
```

# Spring扩展点

## 1. BeanFactoryPostProcessor

<strong style="color:red">改变bean默认的实例化方式</strong>

`postProcessBeanFactory` 该方法执行于bean实例化前，`BeanDefinition` 注册完后，因此在该方法中可以同步beanFactory获取到 `BeanDefinition`，从而改变默认的实例化方式  

```java
@Component
public class BeanFactoryPostProcessorExtension implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// 获取指定的beanDefinition，这里操作bean定义的属性是最合适不过的
		BeanDefinition personDefinition = beanFactory.getBeanDefinition("person");
		System.out.println(personDefinition);
		// 这里建议不要调用getBean来获取某个类的实例进行一些操作，因为这里get的话，他可能还没有进行属性赋值
	}
}
```

## 2. BeanDefinitionRegistryPostProcessor

<strong style="color:red">将指定类注册到BeanFactroy中</strong>

```java
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {
   /**
    * 在标准初始化后修改应用程序上下文的内部bean定义注册表。所有的常规bean定义都将被加载，
    * 但是还没有bean被实例化。这允许在下一个后处理阶段开始之前添加更多的bean定义。
    * */
   void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}
```