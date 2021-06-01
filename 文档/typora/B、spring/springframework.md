[SpringIOC源码解析(上)](https://mp.weixin.qq.com/s?__biz=MzU5MDgzOTYzMw==&mid=2247484561&idx=1&sn=a7281dae7aaaa3499d59dec730464e63&scene=21#wechat_redirect)

[SpringIOC源码解析(下)](https://mp.weixin.qq.com/s?__biz=MzU5MDgzOTYzMw==&mid=2247484564&idx=1&sn=84bd8fee210c0d00687c3094431482a7&scene=21#wechat_redirect)

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