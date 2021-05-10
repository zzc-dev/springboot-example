# 一、内部类

<strong style="color:red">普通的内部类就相当于外部类的一个成员变量或函数，在内部类中可以访问外部的所有函数和方法</strong>

## 1.1 作用：
- 给内部类加上private，可以限制除开外部类之外的访问，用于隐藏内部实现

- 内部类与外部类可以方便地访问彼此的私有属性和方法

- 可以实现多继承的效果（外部类拥有内部类的实例）

    多个内部类实现或者继承不同的类或接口

    多个内部类实现或者继承一个相同的类或接口

## 1.2 成员内部类

1. 相当于一个外部类的普通成员变量，可以随意访问外部类变量和方法

2. **成员内部类中不能存在任何 static 的变量和方法,可以定义常量**:

   ​	**非静态内部类是要依赖于外部类的实例**,而静态变量和方法是不依赖于对象的,仅与类相关,
   简而言之:在加载静态域时,根本没有外部类,所在在非静态内部类中不能定义静态域或方法,编译不通过;
   ​	非静态内部类的作用域是实例级别
   ​	常量是在编译器就确定的,放到所谓的常量池了

3. 实例化内部类：`Inner in = out.new Inner();`

4. 如果外部类和内部类具有相同的成员变量或方法，内部类默认访问自己的成员变量或方法，访问外部类的方式：`Outer.this.name`

## 1.3 静态内部类

**静态内部类的创建不需要依赖外部类**

1. 静态内部类不能直接访问外部类的非静态成员，但可以通过 new 外部类().成员 的方式访问 
2. 如果外部类的静态成员与内部类的成员名称相同，可通过“类名.静态成员”访问外部类的静态成员；
   如果外部类的静态成员与内部类的成员名称不相同，则可通过“成员名”直接调用外部类的静态成员
3. 创建静态内部类的对象时，不需要外部类的对象，`Outer.Inner inner = new Outer.Inner();`

## 1.4 方法内部类

1. 该内部类只属于这个方法，在其他任何地方都无法访问
2. 该内部类不允许有任意权限访问修饰符
3. 方法内部类如果想要使用方法形参，该形参必须使用final声明（JDK8形参变为隐式final声明）[effectively final](# 二、Effectively Final)

## 1.5 匿名内部类

是一个没有名字的方法内部类。除了方法内部类的特点外，还有以下：
    -> 匿名内部类必须实现一个接口或者继承一个抽象类
    -> 它没有名字，因此没有构造函数
应用场景：
    接口、抽象类使用：相当于不用特意去写一个类去实现这个接口的方法，直接在实例化的时候就写好这个方法（接口、抽象类不能实例化，所以采用匿名内部类的方式来写）
    作为参数传递，实现类似多态的效果

# 二、Effectively Final

```java
        /*
		使用的形参为何要为 final???
		 在内部类中的属性和外部方法的参数两者从外表上看是同一个东西，但实际上却不是，所以他们两者是可以任意变化的，
		 也就是说在内部类中我对属性的改变并不会影响到外部的形参，然而这从程序员的角度来看这是不可行的，
		 毕竟站在程序的角度来看这两个根本就是同一个，如果内部类该变了，而外部方法的形参却没有改变这是难以理解
		 和不可接受的，所以为了保持参数的一致性，就规定使用 final 来避免形参的不改变
		 */
		public class Outer{
			public void Show(){
				final int a = 25;
				int b = 13;
				class Inner{
					int c = 2;
					public void print(){
						System.out.println("访问外部类:" + a);
						System.out.println("访问内部类:" + c);
					}
				}
				Inner i = new Inner();
				i.print();
			}
			public static void main(String[] args){
				Outer o = new Outer();
				o.show();
			}
		}    
```

jdk8前，

​	当方法被调用运行完毕之后，局部变量就已消亡了。但内部类对象可能还存在,直到没有被引用时才会消亡。此时就会出现一种情况，就是内部类要访问一个不存在的局部变量;

​	使用final修饰符不仅会保持对象的引用不会改变,而且编译器还会持续维护这个对象在回调方法中的生命周期.

​	局部内部类并不是直接调用方法传进来的参数，而是内部类将传进来的参数通过自己的构造器备份到了自己的内部，自己内部的方法调用的实际是自己的属性而不是外部类方法的参数;

​	防止被篡改数据,而导致内部类得到的值不一致

`>=jdk8`

​	**方法内部类中调用方法中的局部变量,可以不需要修饰为 final,匿名内部类也是一样的，主要是JDK8之后增加了 Effectively final 功能**

# 三、初始化顺序

初始化过程是这样的： 

1.首先，初始化父类中的静态成员变量和静态代码块，按照在程序中出现的顺序初始化； 

2.然后，初始化子类中的静态成员变量和静态代码块，按照在程序中出现的顺序初始化； 

3.其次，初始化父类的普通成员变量和代码块，在执行父类的构造方法；

4.最后，初始化子类的普通成员变量和代码块，在执行子类的构造方法； 

> 字节码中，在链接的准备阶段对静态变量初始化默认值（null或0） 
>
> ​	-> 在初始化阶段clinit中对静态变量按代码出现顺序赋值（赋值语句和static静态代码块）
>
> ​	-> 构造方法执行（init）：普通成员变量的初始化：普通成员变量赋值语句->代码块->构造器

# 四、泛型

> 泛型：参数化类型。将类型由原来的具体类型参数化
>
>    限定了集合的元素类型；获取时避免强制转换。（集合添加后不会记住具体的类型，获取时都是Object）

- 泛型只是在逻辑上是不同的，只作用在编译期，编译后的字节码文件是没有泛型的。**泛型擦除机制**
- 泛型是没有父子关系的。`Box<Integer>`和`Box<Number>`没有父子关系。可使用通配符`?`
- `?`不是类型形参，而是类型实参。它代表了所有<具体类型实参>的父类 
- 类型通配符上限`<? extend Object>`；类型通配符下限`<? super Object>`
- 没有泛型数组的概念

## 4.1 泛型数组

**数组类型检查机制**

> **数组在创建时就确定了元素的类型，并且会记住该类型，每次向数组中添加值时，都会做类型检查，类型不匹配就会抛异常**`java.lang.ArrayStoreException`

```java
Integer[] intArr = new Integer[1];
Object[] objArr = intArr;
objArr[0] = "xxx"; // java.lang.ArrayStoreException
```

泛型数组：

```java
A<String>[] arr = new A<String>[1]; // 编译错误
```

如果编译成功存在的问题：

​	泛型存在擦除，下面代码是没有问题的，`A<String>`、`A<Integer>`在运行时是一样的，这样跳过了**数组类型检查机制**，在使用中会造成**类型转换异常**

```java
A<String>[] arr = new A<String>[1];
Object[] objArr = arr;
objArr[0] = new A<Integer>();
```

**强转**

```java
A<String>[] arr = (A<String>[]) new A[1];
Object[] objArr = arr;
objArr[0] = new A<Integer>(1);
A<String> a = arr[0];
// java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
String s = a.getValue();
```

# 五、注解

注解也叫元数据

注解处理器：反射处理

## 5.1 分类

- **java自带的标准注解**
  - @Override（标明重写某个方法）
  - @Deprecated（标明某个类或方法过时）
  - @SuppressWarnings（标明要忽略的警告）
- **元注解：定义注解的注解**
  - @Retention（标明注解被保留的阶段）
  - @Target（标明注解使用的范围）
  - @Inherited（标明注解可继承）
  - @Documented（标明是否生成javadoc文档）
- **自定义注解**

## 5.2 用途

和XML比较：

​	注解：是一种分散式的元数据，与源代码紧绑定。

​	xml：**是一种集中式的元数据，与源代码无绑定**

1. **生成文档，通过代码里标识的元数据生成javadoc文档。**
2. **编译检查，通过代码里标识的元数据让编译器在编译期间进行检查验证。**
3. **编译时动态处理，编译时通过代码里标识的元数据动态处理，例如动态生成代码。**
4. **运行时动态处理，运行时通过代码里标识的元数据动态处理，例如使用反射注入实例**

## 5.3 @Inherited

@Inherited 元注解是一个标记注解，@Inherited阐述了某个被标注的类型是被继承的。

如果一个使用了@Inherited修饰的annotation类型被用于一个class，则这个annotation将被用于该class的子类

```java

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MyAnTargetType {
    /**
     * 定义注解的一个元素 并给定默认值
     * @return
     */
    String value() default "我是定义在类接口枚举类上的注解元素value的默认值";
}


public class ChildAnnotationTest extends AnnotationTest {
    public static void main(String[] args) {
        // 获取类上的注解MyAnTargetType
        MyAnTargetType t = ChildAnnotationTest.class.getAnnotation(MyAnTargetType.class);
        System.out.println("类上的注解值 === "+t.value());
    }
}
```

