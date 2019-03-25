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

##### 1.配置文件封装

​	spring的配置文件读取是通过ClassPathResource进行封装的，在java中，将不同来源的资源抽象成URL，通过注册不同的handler处理不同来源的资源的读取逻辑，一般的handler的类型使用不同的前缀来标识，然而没有默认定义相对Classpath或ServletContext等资源的handler，虽然可以自己注册实现，但需要了解URL实现机制，且URL没有提供一些基本方法，如检查当前资源是否存在，是否可读等，因而Spring对其内部使用到的资源是吸纳了自己的抽象结构，Resource接口来封装底层资源。

Resource接口抽象了所有Spring内部使用到的资源：File、URL、Classpath等，提供判断当前资源的3个方法：存在性、可读性和是否打开，还提供了不同资源到URL、URI、File类型转换，对不同来源的资源文件都有相应的实现：文件FileSystemResource、Classpath ClasspathResource、URL资源UrlResource等。

##### 2.解析Bean

​	spring初始化有若干种，这里分析使用Resource实例作为参数，

```java
public XmlBeanFactory(Resource resource) throws BeansException {
   this(resource, null);
}
//parentBeanFactory为父类BeanFactory用于Factory用于合并，可以为空，
public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(resource);
	}
```

​	this.reader.loadBeanDefinitions(resource)才是加载资源的真正实现，super(parentBeanFactory)中ignoreDependencyInterface方法，主要忽略给定接口的自动装配功能，官方解释：自动装配时忽略给定的依赖接口，典型应用是通过其他方式解析application上下文注册依赖，类似于BeanFactory通过BeanFactory进行注入或者ApplicationContext通过ApplicationContextAware进行注入。

loadBeanDefinitions处理过程：

1. 封装资源文件，对Resource使用EncodedResource进行封装，
2. 获取输入流，从Resource中获取对应的InputStream并构造InputSource。
3. 通过构造的inputSource实例和Resource实例继续调用doLoadBeanDefinitions

doLoadBeanDefinitions处理步骤：

1. 获取XML文件的验证模式，
2. 加载XML文件，并得到对应的Document，
3. 根据返回的Document注册bean信息

###### 2.1 获取XML的验证模式

DTD与XSD

DTD（Document Type Definition）文档类型定义，一种XML约束模式语言和XML文件的验证机制，属于XML文件的组成部分，包括：元素的定义规则，元素间关系的定义规则，元素可使用的属性，可使用的实体或符号规则。

XSD（XML Schemas Definition），描述xml文档的结构，可以用来验证某个XML文档是否符合其要求，使用XSD对xml实例文档进行检验时，需要声明名称空间和XSD文档的存储位置。

Spring检验模式通过判断是否包含DOCTYPE，包含就是DTD，否则是XSD，

###### 2.2 获取Document

经过验证就可以进行加载Document，XmlBeanFactoryReader委托DocumentLoader执行，真正调用的DefaultDocumentLoader  通过SAX解析XML文档，

```java
public Document loadDocument(InputSource inputSource, EntityResolver entityResolver,
      ErrorHandler errorHandler, int validationMode, boolean namespaceAware) throws Exception {

   DocumentBuilderFactory factory = createDocumentBuilderFactory(validationMode, namespaceAware);
   if (logger.isTraceEnabled()) {
      logger.trace("Using JAXP provider [" + factory.getClass().getName() + "]");
   }
   DocumentBuilder builder = createDocumentBuilder(factory, entityResolver, errorHandler);
   return builder.parse(inputSource);
}
```

EntityResolver

​	如果SAX应用程序需要实现自定义处理外部实体，则必须实现此接口并使用setEntityResolve方法向SAX驱动器注册一个实例。对于解析一个XML，SAX需要读取该XML的DTD，默认是通过网络下载，会出现中断、不可用情况，EntityResolver作用是项目本身就可以提供一个如何寻找DTD声明的方法，避免网络寻找相应的声明，需接受两个参数 publicId和systemId。

###### 2.3 解析及注册BeanDefinition

```java
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
//使用DefaultBeanDefinitionDocumentReader实例化
   BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
   int countBefore = getRegistry().getBeanDefinitionCount();
    //加载及注册bean
   documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
   return getRegistry().getBeanDefinitionCount() - countBefore;
}
```

在registerBeanDefinitions调用doRegisterBeanDefinitions进行解析

```java
protected void doRegisterBeanDefinitions(Element root) {
   BeanDefinitionParserDelegate parent = this.delegate;
   this.delegate = createDelegate(getReaderContext(), root, parent);

   if (this.delegate.isDefaultNamespace(root)) {
       //处理profile 配置环境
      String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
      if (StringUtils.hasText(profileSpec)) {
         String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
               profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
            if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
            if (logger.isDebugEnabled()) {
               logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                     "] not matching: " + getReaderContext().getResource());
            }
            return;
         }
      }
   }

   preProcessXml(root);
   //解析并注册 BeanDefinition
   parseBeanDefinitions(root, this.delegate);
   postProcessXml(root);

   this.delegate = parent;
}
```
###### 默认标签解析

```java
org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#parseDefaultElement

private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
   //对import解析
   if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
      importBeanDefinitionResource(ele);
   }
   //对alias解析
   else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
      processAliasRegistration(ele);
   }//对bean
   else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
      processBeanDefinition(ele, delegate);
   }//解析beans
   else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
      // recurse
      doRegisterBeanDefinitions(ele);
   }
}
```

```java
//对bean 标签进行解析
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
   //获取name和id，判断bean是否指定beanName，没有则设置beanName属性， 解析其他属性封装到GenericBeanDefinition实例中，获取属性封装成bdHolder对象
    BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
   if (bdHolder != null) {
       //解析用户自定义的属性
      bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
      try {
         // Register the final decorated instance.
          //根据bean name、别名 注册实例，会检验是否已经存在相同name的实例
         BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
      }
      catch (BeanDefinitionStoreException ex) {
         getReaderContext().error("Failed to register bean definition with name '" +
               bdHolder.getBeanName() + "'", ele, ex);
      }
      // Send registration event. 通知监听器
      getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
   }
}
```

大致流程：

1）使用parseBeanDefinitionElement方法进行元素解析，将配置文件中的class、name、id、alias封装在bdHolder实例返回。

2）判断默认的子节点下是否有自定义属性，对自定义标签进行解析。

3）注册bdHolder实例

4）发送注册事件，通知监听器，

**BeanDefinition**接口

BeanDefinition是配置文件**<Bean>**元素标签在容器中的内部表现形式，有着对应的属性关系，实现类有：RootBeanDefinition、ChildBeanDefinition和GenericBeanDefinition，都继承了AbstractBeanDefinition，

分别是对应定义的父<bean>和子<bean>,GenericBeanDefinition为bean配置文件属性定义类。

Spring通过BeanDefinition将配置文件中的<Bean>配置信息转换为容器的内部表示，并将这些BeanDefinition注册到BeanDefinitionRegister中，BeanDefinitionRegister就是spring配置信息的内存数据库，主要以map形式保存，后续操作直接从BeanDefinitionRegister中读取配置信息。类图：

![](E:\works\springframework\img\BeanDefinition.png)



**注册BeanDefinition**

```java
public static void registerBeanDefinition(
      BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
      throws BeanDefinitionStoreException {

   // Register bean definition under primary name. 使用bean name做唯一标识注册
   String beanName = definitionHolder.getBeanName();
   registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

   // Register aliases for bean name, if any. 注册所有别名
   String[] aliases = definitionHolder.getAliases();
   if (aliases != null) {
      for (String alias : aliases) {
         registry.registerAlias(beanName, alias);
      }
   }
}
```

根据bean name 注册，registerBeanDefinition主要逻辑：

1）对AbstractBeanDefinition进行校验，验证methodOverrides属性。

2）对BeanName已经注册的情况，如果不允许bean的覆盖，则抛出异常，否则覆盖。

3）加入map缓存。

4）清除之前留下的beanName的缓存。

根据别名注册，registry.registerAlias(beanName, alias);

1）alias与beanName相同时，不处理并删除原理的alias，

2）覆盖处理，

3）循环检查，当存在A->B时，若再次出现A->C->B时，则抛出异常。

**通知监听器解析及注册完成**

spring没有对此事件做任何逻辑处理，开发人员可以自行实现逻辑。

###### 解析自定义标签

自定义标签的使用

1）创建一个需要扩展的组件

2）定义一个XSD文件描述组件内容

3）创建一个类，实现BeanDefinitionParser接口，解析XSD文件中的定义和组件定义

4）创建一个Handler文件，扩展NamespaceHandlerSupport，目的是将组件祖册到spring容器

5）编写Spring.handler和Spring.schemas文件

解析自定义标签

```java
public BeanDefinition parseCustomElement(Element ele, @Nullable BeanDefinition containingBd) {
    //获取标签的命名空间
   String namespaceUri = getNamespaceURI(ele);
   if (namespaceUri == null) {
      return null;
   }
    //获取自定义标签处理器
   NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
   if (handler == null) {
      error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
      return null;
   }
    //使用NamespaceHandlerSupport的parse方法，注册BeanDefinitionHolder并发送通知事件，
   return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
}
```

3.加载Bean

```java
public Object getBean(String name) throws BeansException {
   return doGetBean(name, null, null, false);
}

protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
			@Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
		//转换对应的beanName
		final String beanName = transformedBeanName(name);
		Object bean;

		// 尝试从缓存获取或者singletonFactories中的getObject()
		Object sharedInstance = getSingleton(beanName);
		if (sharedInstance != null && args == null) {
			if (logger.isTraceEnabled()) {
				if (isSingletonCurrentlyInCreation(beanName)) {
					logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
							"' that is not fully initialized yet - a consequence of a circular reference");
				}
				else {
					logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
				}
			}
            //返回对应的实例，若存在BeanFactory，则返回指定方法返回的实例，而不是返回实例本身
			bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		}

		else {
			// Fail if we're already creating this bean instance:
			// We're assumably within a circular reference.
			if (isPrototypeCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			// Check if bean definition exists in this factory.
			BeanFactory parentBeanFactory = getParentBeanFactory();
            //若beanDefinitionMap不存在beanName，则到parentBeanFactory中查找
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// Not found -> check parent.
				String nameToLookup = originalBeanName(name);
				if (parentBeanFactory instanceof AbstractBeanFactory) {
					return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
							nameToLookup, requiredType, args, typeCheckOnly);
				}
				else if (args != null) {
					// Delegation to parent with explicit args.
					return (T) parentBeanFactory.getBean(nameToLookup, args);
				}
				else if (requiredType != null) {
					// No args -> delegate to standard getBean method.
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
				else {
					return (T) parentBeanFactory.getBean(nameToLookup);
				}
			}

			if (!typeCheckOnly) {
				markBeanAsCreated(beanName);
			}

			try {
                //返回合并的RootBeanDefinition,如果指定的beanName 是子bean的话，会合并到父类的相关属性
				final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				checkMergedBeanDefinition(mbd, beanName, args);

				// Guarantee initialization of beans that the current bean depends on.
				String[] dependsOn = mbd.getDependsOn();
                //若存在依赖时则递归实例化依赖bean
				if (dependsOn != null) {
					for (String dep : dependsOn) {
						if (isDependent(beanName, dep)) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
						}
						registerDependentBean(dep, beanName);
						try {
							getBean(dep);
						}
						catch (NoSuchBeanDefinitionException ex) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
						}
					}
				}

				// Create bean instance.实例化依赖的bean后，开始实例化mbd本身，
                //单例模式创建
				if (mbd.isSingleton()) {
					sharedInstance = getSingleton(beanName, () -> {
						try {
							return createBean(beanName, mbd, args);
						}
						catch (BeansException ex) {
							// Explicitly remove instance from singleton cache: It might have been put there
							// eagerly by the creation process, to allow for circular reference resolution.
							// Also remove any beans that received a temporary reference to the bean.
							destroySingleton(beanName);
							throw ex;
						}
					});
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				}

				else if (mbd.isPrototype()) {
					// It's a prototype -> create a new instance.
					Object prototypeInstance = null;
					try {
						beforePrototypeCreation(beanName);
						prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						afterPrototypeCreation(beanName);
					}
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				}

				else {
					String scopeName = mbd.getScope();
					final Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
					}
					try {
						Object scopedInstance = scope.get(beanName, () -> {
							beforePrototypeCreation(beanName);
							try {
								return createBean(beanName, mbd, args);
							}
							finally {
								afterPrototypeCreation(beanName);
							}
						});
						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
					}
					catch (IllegalStateException ex) {
						throw new BeanCreationException(beanName,
								"Scope '" + scopeName + "' is not active for the current thread; consider " +
								"defining a scoped proxy for this bean if you intend to refer to it from a singleton",
								ex);
					}
				}
			}
			catch (BeansException ex) {
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
		}

		// C
		if (requiredType != null && !requiredType.isInstance(bean)) {
			try {
				T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
				if (convertedBean == null) {
					throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
				}
				return convertedBean;
			}
			catch (TypeMismatchException ex) {
				if (logger.isTraceEnabled()) {
					logger.trace("Failed to convert bean '" + name + "' to required type '" +
							ClassUtils.getQualifiedName(requiredType) + "'", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		return (T) bean;
	}
```

梳理下流程图：

![](E:\works\springframework\img\doBean.png)

1)转换对应的beanName

传入的参数可能是别名，也可能是FactoryBean，所以需要进一步解析。

- 去除FactoryBean的修饰符，如name=‘&aa’,解析为name='aa'
- 去指定alias表示的最终beanNa

2）尝试从缓存中加载单例