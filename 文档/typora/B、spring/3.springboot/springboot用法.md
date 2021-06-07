#  一、监听器

```java
// 1.创建监听器实现ApplicationListener<ApplicationEvent>
@Order(Ordered.HIGHEST_PRECEDENCE)              
 public class HelloWorldApplicationListener implements ApplicationListener<ApplicationEvent>{
   @Override
   public void onApplicationEvent(ApplicationEvent event) {
     System.out.println("helloWorld first" + event.getClass().getName());
   }
 }

// 2.在main中向容器添加该监听器
springApplication.addListeners(new HelloWorldApplicationListener());
```

# 二、 ApplicationRunner和CommandLineRunner

在容器启动完成前运行一些特定的代码，需要实现上面两个接口的任一个。具体运行时机是在ApplicationStartedEvent和ApplicationReadyEvent之间。

以CommandLineRunner为例

![image-20210607194801705](D:\myself\springboot-example\文档\typora\images\springboot05.png)

 

# 三、自定义HttpMessageConvert

1. 实现自定义转换器SettingConvert继承AbstractHttpMessageConvert

2. 将自定义转换器加入[HttpMessageConverter](#_1._SettingConvert)中

![image-20210607194920398](D:\myself\springboot-example\文档\typora\images\springboot06.png)

# 四、@ControllerAdvice

```java
@ControllerAdvice(basePackageClasses = AcmeController.class)
public class AcmeControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(YourException.class)
    @ResponseBody
    ResponseEntity<?> handleControllerException(HttpServletRequest request, Throwable ex) {
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(new CustomErrorType(status.value(), ex.getMessage()), status);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }
}
```

