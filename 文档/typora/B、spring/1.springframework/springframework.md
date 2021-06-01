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