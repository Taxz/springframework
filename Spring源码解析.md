# Spring源码解析

[TOC]



## 第一章 Spring整体架构和环境搭建

​	Spring是于2003年兴起的一个轻量级的java开源框架，由Rod Johnson在其著作《Expert One-On-One J2EE Development and Design》中阐述的部分理念和原型衍生而来的。

### 1.spring的整体架构

spring框架是一个分层架构，包含一系列功能的要素，并被分为大约20个模块，如图

![](C:\Users\Administrator\Desktop\springFramework.png)

模块介绍：

#### 1.Core Container

核心容器包含有Core、Beans、Context和Expression Language模块，Core和Beans模块是框架的基础部分，提供IoC和依赖注入特性。

- Core模块主要包含spring框架基本的和兴工具类，是其他组件的基本核心。

- Beans模块是所有应用都要用到的，包含访问配置文件、创建和管理bean以及进行Inversion of Control/Dependency Injection 操作相关的所有类。

- Context模块构建于Core和Beans模块基础之上，提供了一种类似于JNID注册器的框架式的对象访问方法，Context模块继承了Beans的特性，为Spring核心提供大量扩展，添加了对国际化、事件传播、资源加载和对Context的透明创建的支持。

- Expression Language 模块提供了一个强大的表达式语言用于在运行时查询和操纵对象。

#### 2.Data Access/Integration

JDBC 模块提供了一个JDBC抽象层，包含了对JDBC数据访问进行封装的所有类。

- ORM 模块为流行的对象-关系映射API，

- OXM 模块提供了一个对Object/XML映射实现的抽象层，Object/XML包括JAXB、Castor、XMLBeans、JiBX和Stream。

- JMS(java Messaging Service)模块主要包含了一些生成和消费消息的特性。

- Transaction模块支持编程和声明性的事务管理。

#### 3.Web

Web上下文模块建立在应用程序上下文模块之上，为基于Web的应用程序提供上下文，

- Web模块：提供了基础的面向Web的集成特性，如多文件上传，使用servlet listen二十初始化IoC容器以及一个面向Web的应用上下文。

- servlet：包含Spring的model-view-Controller(MVC)实现，Spring的MVC框架是的模型范围内的代码和web forms之间能够清楚地分离开来，并与Spring框架的其他特性集成在一起。

- Struts：该模块提供了对Struts支持，

- Prolet：提供了用于Porlet环境和Servlet模块的MVC的实现。

#### 4.AOP

该模块提供了一个符合AOP联盟标准的面向切面编程的实现，它让你可以定义例如方法拦截器和切点，从而将逻辑代码分开，降低他们之间的耦合性。

- Aspects提供了对AspectJ的集成支持。

- Instrumentation提供了class instrumentation支持和classloader实现，是的可以在特定的应用服务器上使用。

#### 5.Test

- Test模块支持试用JUnit和TestNG对spring组件进行测试。

## 第二章 容器的基本实现

### 1.spring的结构组成

#### 1.核心类介绍

##### 1)DefaultListableBeanFactory 与XmlBeanFactory

XmlBeanFactory继承自DefaultListableBeanFactory，DefaultListableBeanFactory是整个bean加载的核心部分，是spring注册和加载bean的默认实现，不同之处是XmlBeanFactory使用自定义的XML读取器XmlBeanDefinitionReader，实现了个性化的BeanDefinitionReader读取，获取和注册bean使用从父类中继承的方法实现。DefaultListableBeanFactory 类图：

![](C:\Users\Administrator\Desktop\DefaultListableBeanFactory.PNG)

##### 2)XmlBeanDefinitionReader

​	XML配置文件的读取是spring中重要的功能，因为Spring中的大部分功能都是以配置作为切入点的，XmlBeanDefinitionReader读取xml的流程包含以下几步：

- 通过继承AbstractBeanDefinitionReader中的方法，来使用ResourceLoader将资源文件路径转换为对应的Resource文件，

- 通过DocumentLoader对Resource文件进行转换，将Resource文件转换为Document文件。

- 通过实现BeanDefinitionDocumentReader的DocumentDefinitionDocumentReader类对Document进行解析，并使用DefaultBeanDefinitionParserDelegate类对Element进行解析，



#### 2.容器的基础XmlBeanFactory

1.配置文件封装

spring的配置文件读取是通过ClassPathResource进行封装的，在java中，将不同来源的资源抽象成URL，通过注册不同的handler处理不同来源的资源的读取逻辑，一般的handler的类型使用不同的前缀来标识，然而没有默认定义相对Classpath或ServletContext等资源的handler，虽然可以自己注册实现，但需要了解URL实现机制，且URL没有提供一些基本方法，如检查当前资源是否存在，是否可读等，因而Spring对其内部使用到的资源是吸纳了自己的抽象结构，Resource接口来封装底层资源。

Resource接口抽象了所有Spring内部使用到的资源：File、URL、Classpath等，提供判断当前资源的3个方法：存在性、可读性和是否打开，还提供了不同资源到URL、URI、File类型转换，对不同来源的资源文件都有相应的实现：文件FileSystemResource、Classpath ClasspathResource、URL资源UrlResource等。

2.加载Bean

