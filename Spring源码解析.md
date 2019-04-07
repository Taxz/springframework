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

org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean
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

​	单例在spring容器中只会创建一次，后续直接存缓存中获取，

```java
public Object getSingleton(String beanName) {
	//true 允许早起依赖
   return getSingleton(beanName, true);
}

protected Object getSingleton(String beanName, boolean allowEarlyReference) {
	//检查缓存(map)中是否存在	
    Object singletonObject = this.singletonObjects.get(beanName);
    //缓存中不存在，且该实例正在创建
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            //锁定全局变量
			synchronized (this.singletonObjects) {
                //该实例正在加载则不处理
				singletonObject = this.earlySingletonObjects.get(beanName);
				if (singletonObject == null && allowEarlyReference) {
                    //当某些方法需要提前初始化的时候则会调用addSingletonFactory方法将对应的ObjectFactory初始化策略存储在singletonFactories中
					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
                        //singletonFactory.getObject() 返回对象
						singletonObject = singletonFactory.getObject();
                        //其目的是用来检测循环引用
						this.earlySingletonObjects.put(beanName, singletonObject);
                        //earlySingletonObjects与singletonFactories互斥
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		return singletonObject;
	}
```

```
singletonObjects：beanName与创建bean实例之间的关系
singletonFactories：beanName与创建工厂之间的关系
earlySingletonObjects：保存在beanName和创建bean实例之间的关系，当一个bean被放入后，那么当bean还在创建过程中，就可以通过getBean方法获取，其目的是用来检测循环引用
registeredSingletons:用来保存当前所有已经注册的bean
```

3）bean的实例化

​	如果从缓存中获取到了bean原始状态，则需要进行实例化，由getObjectForBeanInstance完成。

```java
protected Object getObjectForBeanInstance(
      Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {

   // 如果指定的name是工厂前缀(&)
   if (BeanFactoryUtils.isFactoryDereference(name)) {
      if (beanInstance instanceof NullBean) {
         return beanInstance;
      }
       //验证不通过
      if (!(beanInstance instanceof FactoryBean)) {
         throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
      }
   }

   //该实例可能是正常的bean或者是FactoryBean
    //判断用户通过前缀获取直接获取工厂实例
   if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
      return beanInstance;
   }

    //加载FactoryBean
   Object object = null;
   if (mbd == null) {
       //尝试从缓存中加载
      object = getCachedObjectForFactoryBean(beanName);
   }
   if (object == null) {
      // Return bean instance from factory. 到这里可以确定实例一定是FactoryBean类型
      FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
      // Caches object obtained from FactoryBean if it is a singleton.在所有已经加载的类中检查是否定义beanName
      if (mbd == null && containsBeanDefinition(beanName)) {
          //转为RootBeanDefinition，
         mbd = getMergedLocalBeanDefinition(beanName);
      }
       /**
	 * Return whether this bean definition is 'synthetic', that is,
	 * not defined by the application itself.
	 */
      boolean synthetic = (mbd != null && mbd.isSynthetic());
      object = getObjectFromFactoryBean(factory, beanName, !synthetic);
   }
   return object;
}

protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
    //如果是单例模式
		if (factory.isSingleton() && containsSingleton(beanName)) {
			synchronized (getSingletonMutex()) {
				Object object = this.factoryBeanObjectCache.get(beanName);
				if (object == null) {
					object = doGetObjectFromFactoryBean(factory, beanName);
					// Only post-process and store if not put there already during getObject() call above
					// (e.g. because of circular reference processing triggered by custom getBean calls)
					Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
					if (alreadyThere != null) {
						object = alreadyThere;
					}
					else {
						if (shouldPostProcess) {
							if (isSingletonCurrentlyInCreation(beanName)) {
								// Temporarily return non-post-processed object, not storing it yet..
								return object;
							}
							beforeSingletonCreation(beanName);
							try {
								object = postProcessObjectFromFactoryBean(object, beanName);
							}
							catch (Throwable ex) {
								throw new BeanCreationException(beanName,
										"Post-processing of FactoryBean's singleton object failed", ex);
							}
							finally {
								afterSingletonCreation(beanName);
							}
						}
						if (containsSingleton(beanName)) {
							this.factoryBeanObjectCache.put(beanName, object);
						}
					}
				}
				return object;
			}
		}
		else {
			Object object = doGetObjectFromFactoryBean(factory, beanName);
			if (shouldPostProcess) {
				try {
					object = postProcessObjectFromFactoryBean(object, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
				}
			}
			return object;
		}
	}

private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName)
			throws BeanCreationException {
		Object object;
		try {
            //权限验证
			if (System.getSecurityManager() != null) {
				AccessControlContext acc = getAccessControlContext();
				try {
					object = AccessController.doPrivileged((PrivilegedExceptionAction<Object>) factory::getObject, acc);
				}
				catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			}
			else {
                //如果bean声明为factoryBean类型，则提取bean时	并不是FactoryBean,而是对应的getObject返回的bean
				object = factory.getObject();
			}
		}
		catch (FactoryBeanNotInitializedException ex) {
			throw new BeanCurrentlyInCreationException(beanName, ex.toString());
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
		}

		// Do not accept a null value for a FactoryBean that's not fully
		// initialized yet: Many FactoryBeans just return null then.
		if (object == null) {
			if (isSingletonCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(
						beanName, "FactoryBean which is currently in creation returned null from getObject");
			}
			object = new NullBean();
		}
		return object;
	}
```

```java
org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#postProcessObjectFromFactoryBean
protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
   return applyBeanPostProcessorsAfterInitialization(object, beanName);
}
```

4）原型模式的依赖检查

​	只有在单例情况下才会尝试解决循环依赖，

5）检测parentBeanFactory

​	若当前xml配置文件不包含beanName所对应的配置，只能尝试到父类加载

6）合并为RootBeanDefinition

​	从xml读取的配置存在GenericBeanDefinition中，转换为RootBeanDefinition进行后续操作，若父类不为空，则会合并到父类的属性，

7）实例化依赖

8）针对不同的scope进行bean创建

```java
....
 //单例  加载bean
if (mbd.isSingleton()) {
   sharedInstance = getSingleton(beanName, () -> {
      try {
          //作为回调
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
.....

org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#getSingleton(java.lang.String, org.springframework.beans.factory.ObjectFactory<?>)
public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(beanName, "Bean name must not be null");
    //获取全局变量锁
		synchronized (this.singletonObjects) {
            //检查是否已经加载过
			Object singletonObject = this.singletonObjects.get(beanName);
            //实例化
			if (singletonObject == null) {
				if (this.singletonsCurrentlyInDestruction) {
					throw new BeanCreationNotAllowedException(beanName,
							"Singleton bean creation not allowed while singletons of this factory are in destruction " +
							"(Do not request a bean from a BeanFactory in a destroy method implementation!)");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
				}
                //记录加载状态，以便对循环依赖检查
				beforeSingletonCreation(beanName);
				boolean newSingleton = false;
				boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
				if (recordSuppressedExceptions) {
					this.suppressedExceptions = new LinkedHashSet<>();
				}
				try {
                    //初始化  回调
					singletonObject = singletonFactory.getObject();
					newSingleton = true;
				}
				catch (IllegalStateException ex) {
					// Has the singleton object implicitly appeared in the meantime ->
					// if yes, proceed with it since the exception indicates that state.
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						throw ex;
					}
				}
				catch (BeanCreationException ex) {
					if (recordSuppressedExceptions) {
						for (Exception suppressedException : this.suppressedExceptions) {
							ex.addRelatedCause(suppressedException);
						}
					}
					throw ex;
				}
				finally {
					if (recordSuppressedExceptions) {
						this.suppressedExceptions = null;
					}
					afterSingletonCreation(beanName);
				}
				if (newSingleton) {
                    //将结果加入缓存并删除加载bean过程中记录的辅助状态
					addSingleton(beanName, singletonObject);
				}
			}
			return singletonObject;
		}
	}
```

9）类型转换

将返回的bean转换为requireType所指定的类型

**FactoryBean**

org.springframework.beans.factory.FactoryBean

```java
public interface FactoryBean<T> {
    //返回由FactoryBean创建的bean实例,如果isSingleton返回为true，则将该实例放到spring容器中单实例缓存中
T getObject() throws Exception;
    //返回创建bean的类型
Class<?> getObjectType();
    //返回由FactoryBean创建bean实例的作用域
default boolean isSingleton() {
		return true;
	}
}
```

FactoryBean	对于spring框架来说占用重要的地位，spring自身有70多种实现，他们隐藏了实例化一些复杂bean的细节，给上层应用带来便利，从spring3.0 FactoryBean接口支持泛型。

当配置文件中的<bean>的class属性配置的实现类是FactoryBean时，通过getBean()返回的不是FactoryBean本身，而是FactoryBean#getObject()方法所返回的对象，相当于FactoryBean#getObject()代理了getBean()方法。

**准备创建Bean**

```java
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
      throws BeanCreationException {

   if (logger.isTraceEnabled()) {
      logger.trace("Creating instance of bean '" + beanName + "'");
   }
   RootBeanDefinition mbdToUse = mbd;
//根据设置的class属性或者className来解析Class，并将解析后的类存储在bean定义中，以供进一步使用。
   Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
   if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
      mbdToUse = new RootBeanDefinition(mbd);
      mbdToUse.setBeanClass(resolvedClass);
   }

   // Prepare method overrides. 对overrides属性进行标记及验证 将lookup和replace-method同意存放在methodOerrides，在其中的prepareMethodOverride方法，判断是否被重载
   try {
      mbdToUse.prepareMethodOverrides();
   }
   catch (BeanDefinitionValidationException ex) {
      throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
            beanName, "Validation of method overrides failed", ex);
   }

   try {
      // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
      Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
       //短路判断
      if (bean != null) {
         return bean;
      }
   }
   catch (Throwable ex) {
      throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
            "BeanPostProcessor before instantiation of bean failed", ex);
   }

   try {
      Object beanInstance = doCreateBean(beanName, mbdToUse, args);
      if (logger.isTraceEnabled()) {
         logger.trace("Finished creating instance of bean '" + beanName + "'");
      }
      return beanInstance;
   }
   catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
      // A previously detected exception with proper bean creation context already,
      // or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
      throw ex;
   }
   catch (Throwable ex) {
      throw new BeanCreationException(
            mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
   }
}
```

###### **解决循环依赖**

spring容器将每一个正在创建的bean标示符放在一个“当前创建的池”中，bean标示符在创建过程中将一直保持在这个池中，因此如果在创建bean的过程中发现自己已经在“当前创建的池”中，则抛出BeanCurrentlyInCreationException异常表示循环依赖。

1.构造器循环依赖

此依赖无法解决，只能抛出异常，

```java
<bean id="testA" class="...">
	<constructor-arg index="0" ref="testB"/>
</bean>
<bean id="testB" class="...">
	<constructor-arg index="0" ref="testC"/>
</bean>
<bean id="testC" class="...">
	<constructor-arg index="0" ref="testA"/>
</bean>

创建过程
1.spring容器创建testA时，在“当前创建的池”中查找是否有testA，若果没有发现，则继续准备其需要的构造参数“testB”,并将testA放入“当前创建的池”中。
2.spring容器创建testB时，在“当前创建的池”中查找是否有testB，若果没有发现，则继续准备其需要的构造参数“testC”,并将testB放入“当前创建的池”中。
3.spring容器创建testC时，在“当前创建的池”中查找是否有testC，若果没有发现，则继续准备其需要的构造参数“testA”,并将testC放入“当前创建的池”中。
4.此时spring容器创建testA时，发现在“当前创建的池”中查找有testA，则抛出异常
```

2.setter注入方式构成的循环依赖

spring容器通过提前暴露刚完成构造器注入但未完成其他步骤的bean来实现，但只能解决单例作用域的bean循环依赖。通过提前暴露一个单例工厂方法，从而使其他bean能引用到该bean。

```
1.spring容器创建单例testA时，首先根据无参构造起创建bean，并暴露一个“ObjectFactory”用于返回一个提前暴露一个创建中的bean，并将testA标示符放到“当前创建的池”中，然后进行setter注入testB。
2.spring容器创建单例testB时，首先根据无参构造起创建bean，并暴露一个“ObjectFactory”用于返回一个提前暴露一个创建中的bean，并将testB标示符放到“当前创建的池”中，然后进行setter注入testC。
3.spring容器创建单例testC时，首先根据无参构造起创建bean，并暴露一个“ObjectFactory”用于返回一个提前暴露一个创建中的bean，并将testC标示符放到“当前创建的池”中，然后进行setter注入testA,进行注入testA时，由于提前暴露了“ObjectFactory”工厂，从而返回提前暴露一个创建中的bean。
4.最后在依赖注入testB和testA，完成setter注入
```

3.prototype范围的依赖处理

无法完成依赖注入，因为spring容器不进行prototype的作用域缓存，无法提前暴露创建中的bean。

###### **创建bean**

```java
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
      throws BeanCreationException {

   // Instantiate the bean.
   BeanWrapper instanceWrapper = null;
   if (mbd.isSingleton()) {
   //如果是单例，返回实例并清除缓存
      instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
   }
   if (instanceWrapper == null) {
   /** Create a new instance for the specified bean, using an appropriate instantiation strategy:
	 * factory method, constructor autowiring, or simple instantiation.
	 */
      instanceWrapper = createBeanInstance(beanName, mbd, args);
   }
   /**
	 * Return the bean instance wrapped by this object.
	 */
   final Object bean = instanceWrapper.getWrappedInstance();
   	/**
	 * Return the type of the wrapped bean instance.
	 */
   Class<?> beanType = instanceWrapper.getWrappedClass();
   if (beanType != NullBean.class) {
      mbd.resolvedTargetType = beanType;
   }

   // Allow post-processors to modify the merged bean definition.
   synchronized (mbd.postProcessingLock) {
   /** postProcessed  Package-visible field that indicates MergedBeanDefinitionPostProcessor having been applied. */
      if (!mbd.postProcessed) {
         try {
         /**
	 * Apply MergedBeanDefinitionPostProcessors to the specified bean definition,
	 * invoking their {@code postProcessMergedBeanDefinition} methods.
	 */
            applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
         }
         catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                  "Post-processing of merged bean definition failed", ex);
         }
         mbd.postProcessed = true;
      }
   }

   // Eagerly cache singletons to be able to resolve circular references
   // even when triggered by lifecycle interfaces like BeanFactoryAware.
   //是否需要提前曝光：单例&允许循环依赖&当前bean正在创建，检查循环依赖
   boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
         isSingletonCurrentlyInCreation(beanName));
   if (earlySingletonExposure) {
      if (logger.isTraceEnabled()) {
         logger.trace("Eagerly caching bean '" + beanName +
               "' to allow for resolving potential circular references");
      }
      /**
      * 为避免后期循环依赖，可以在bean初始化完成前将创建实例的ObjectFactory加入工厂
      */
      addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
   }

   // Initialize the bean instance.
   Object exposedObject = bean;
   try {
   /**
	 * Populate the bean instance in the given BeanWrapper with the property values
	 * from the bean definition.
	 */
      populateBean(beanName, mbd, instanceWrapper);
      // 调用初始化方法，如init-method
      exposedObject = initializeBean(beanName, exposedObject, mbd);
   }
   catch (Throwable ex) {
      if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
         throw (BeanCreationException) ex;
      }
      else {
         throw new BeanCreationException(
               mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
      }
   }

   if (earlySingletonExposure) {
      Object earlySingletonReference = getSingleton(beanName, false);
      //只有检测到有循环依赖的情况下才会不为空
      if (earlySingletonReference != null) {
      //没有改变就是没有增强
         if (exposedObject == bean) {
            exposedObject = earlySingletonReference;
         }
         else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
            String[] dependentBeans = getDependentBeans(beanName);
            Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
            for (String dependentBean : dependentBeans) {
            //依赖检测
               if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                  actualDependentBeans.add(dependentBean);
               }
            }
            /**因为bean创建后，其所依赖的bean一定是已经创建的，actualDependentBeans不为空表示，当前bean创建后，其所依赖的bean却没有全部建完，就是说存在循环依赖
            if (!actualDependentBeans.isEmpty()) {
               throw new BeanCurrentlyInCreationException(beanName,
                     "Bean with name '" + beanName + "' has been injected into other beans [" +
                     StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                     "] in its raw version as part of a circular reference, but has eventually been " +
                     "wrapped. This means that said other beans do not use the final version of the " +
                     "bean. This is often the result of over-eager type matching - consider using " +
                     "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
            }
         }
      }
   }

   // Register bean as disposable.
   try {
   //注册销毁的方法，当调用工厂的shutdown方法时
      registerDisposableBeanIfNecessary(beanName, bean, mbd);
   }
   catch (BeanDefinitionValidationException ex) {
      throw new BeanCreationException(
            mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
   }

   return exposedObject;
}
```

```java
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
   // Make sure bean class is actually resolved at this point.
   Class<?> beanClass = resolveBeanClass(mbd, beanName);

   if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
      throw new BeanCreationException(mbd.getResourceDescription(), beanName,
            "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
   }
   /**
	 * Return a callback for creating an instance of the bean, if any.
	 * @since 5.0
	 */
   Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
   if (instanceSupplier != null) {
       /**
         * Obtain a bean instance from the given supplier.
         */
      return obtainFromSupplier(instanceSupplier, beanName);
   }

    //不为空则使用工厂方法初始化
   if (mbd.getFactoryMethodName() != null) {
      return instantiateUsingFactoryMethod(beanName, mbd, args);
   }

   // Shortcut when re-creating the same bean...
   boolean resolved = false;
   boolean autowireNecessary = false;
   if (args == null) {
      synchronized (mbd.constructorArgumentLock) {
         if (mbd.resolvedConstructorOrFactoryMethod != null) {
            resolved = true;
            autowireNecessary = mbd.constructorArgumentsResolved;
         }
      }
   }
   if (resolved) {
      if (autowireNecessary) {
         return autowireConstructor(beanName, mbd, null, null);
      }
      else {
         return instantiateBean(beanName, mbd);
      }
   }

   // Candidate constructors for autowiring?
   Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
   if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
         mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
      return autowireConstructor(beanName, mbd, ctors, args);
   }

   /**
	 * Determine preferred constructors to use for default construction, if any.
	 * Constructor arguments will be autowired if necessary.
	 * @return one or more preferred constructors, or {@code null} if none
	 * (in which case the regular no-arg default constructor will be called)
	 * @since 5.1
	 */ 
   ctors = mbd.getPreferredConstructors();
   if (ctors != null) {
       //构造函数自动注入
      return autowireConstructor(beanName, mbd, ctors, null);
   }

   // No special handling: simply use no-arg constructor.
    //使用默认构造函数
   return instantiateBean(beanName, mbd);
}
```

```java
protected BeanWrapper autowireConstructor(
      String beanName, RootBeanDefinition mbd, @Nullable Constructor<?>[] ctors, @Nullable Object[] explicitArgs) {

   return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
}
```

```java
//构造函数自动注入，按照提供的构造函数参数方式
//如果指定了显式构造函数参数值，
//用bean工厂中的bean匹配所有剩余的参数。
//这对应于构造函数注入:在这种模式下，是一个Spring
//bean factory能够托管基于构造函数的组件依赖性解析。
public BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd,
      @Nullable Constructor<?>[] chosenCtors, @Nullable Object[] explicitArgs) {

   BeanWrapperImpl bw = new BeanWrapperImpl();
   this.beanFactory.initBeanWrapper(bw);

   Constructor<?> constructorToUse = null;`
   ArgumentsHolder argsHolderToUse = null;
   Object[] argsToUse = null;
//如果传入的explicitArgs不为空，则直接确定参数，
   if (explicitArgs != null) {
      argsToUse = explicitArgs;
   }
   else {
       //如果传入的explicitArgs为空，
      Object[] argsToResolve = null;
      synchronized (mbd.constructorArgumentLock) {
          //尝试从缓存中获取构造函数
         constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
         if (constructorToUse != null && mbd.constructorArgumentsResolved) {
            // Found a cached constructor...
            //尝试从缓存中获取构造参数
            argsToUse = mbd.resolvedConstructorArguments;
            if (argsToUse == null) {
               argsToResolve = mbd.preparedConstructorArguments;
            }
         }
      }
      if (argsToResolve != null) {
          //从bean定义中解析出参数
         argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse, argsToResolve, true);
      }
   }
	//若构造参数或者构造函数为空
   if (constructorToUse == null || argsToUse == null) {
      // Take specified constructors, if any.
      Constructor<?>[] candidates = chosenCtors;
       //若传入的构造函数为null，则从mbd中获取
      if (candidates == null) {
         Class<?> beanClass = mbd.getBeanClass();
         try {
            candidates = (mbd.isNonPublicAccessAllowed() ?
                  beanClass.getDeclaredConstructors() : beanClass.getConstructors());
         }
         catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                  "Resolution of declared constructors on bean Class [" + beanClass.getName() +
                  "] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
         }
      }
	//如果只有一个构造函数，参数为空，且没有构造参数，则解析并实例化
      if (candidates.length == 1 && explicitArgs == null && !mbd.hasConstructorArgumentValues()) {
         Constructor<?> uniqueCandidate = candidates[0];
         if (uniqueCandidate.getParameterCount() == 0) {
            synchronized (mbd.constructorArgumentLock) {
               mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
               mbd.constructorArgumentsResolved = true;
               mbd.resolvedConstructorArguments = EMPTY_ARGS;
            }
            bw.setBeanInstance(instantiate(beanName, mbd, uniqueCandidate, EMPTY_ARGS));
            return bw;
         }
      }

      // 在确定了构造参数后，需要根据构造参数确定构造函数
      boolean autowiring = (chosenCtors != null ||
            mbd.getResolvedAutowireMode() == AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
      ConstructorArgumentValues resolvedValues = null;

      int minNrOfArgs;
      if (explicitArgs != null) {
          //设置参数个数
         minNrOfArgs = explicitArgs.length;
      }
      else {
         ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
         resolvedValues = new ConstructorArgumentValues();
          //从bean定义中解析出参数个数
         minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
      }
	//根据构造函数按照public构造函数优先参数数量降序、非public构造函数参数数量降序，可以迅速判断排在后面的构造函数参数个数是否符合条件
      AutowireUtils.sortConstructors(candidates);
      int minTypeDiffWeight = Integer.MAX_VALUE;
      Set<Constructor<?>> ambiguousConstructors = null;
      LinkedList<UnsatisfiedDependencyException> causes = null;

      for (Constructor<?> candidate : candidates) {
         Class<?>[] paramTypes = candidate.getParameterTypes();

         if (constructorToUse != null && argsToUse.length > paramTypes.length) {
            // Already found greedy constructor that can be satisfied ->
            // do not look any further, there are only less greedy constructors left.
            break;
         }
         if (paramTypes.length < minNrOfArgs) {
            continue;
         }

         ArgumentsHolder argsHolder;
          //参数值不为空
         if (resolvedValues != null) {
            try {
                //通过注解方式获取参数名
               String[] paramNames = ConstructorPropertiesChecker.evaluate(candidate, paramTypes.length);
               if (paramNames == null) {
                   // 通过ParameterNameDiscoverer 工具获取参数名
                  ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                  if (pnd != null) {
                     paramNames = pnd.getParameterNames(candidate);
                  }
               }
                /**
                *创建一个参数数组来调用构造函数或工厂方法，给定已解析的构造函数参数值。
                */
               argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw, paramTypes, paramNames,
                     getUserDeclaredConstructor(candidate), autowiring, candidates.length == 1);
            }
            catch (UnsatisfiedDependencyException ex) {
               if (logger.isTraceEnabled()) {
                  logger.trace("Ignoring constructor [" + candidate + "] of bean '" + beanName + "': " + ex);
               }
               // Swallow and try next constructor.
               if (causes == null) {
                  causes = new LinkedList<>();
               }
               causes.add(ex);
               continue;
            }
         }
         else {
            // Explicit arguments given -> arguments length must match exactly.
            if (paramTypes.length != explicitArgs.length) {
               continue;
            }
            argsHolder = new ArgumentsHolder(explicitArgs);
         }
//在宽松模式下解析构造函数还是在严格模式下解析构造函数。
         int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
               argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
         // Choose this constructor if it represents the closest match.如果它表示最近的匹配，那么选择这个构造函数。
         if (typeDiffWeight < minTypeDiffWeight) {
            constructorToUse = candidate;
            argsHolderToUse = argsHolder;
            argsToUse = argsHolder.arguments;
            minTypeDiffWeight = typeDiffWeight;
            ambiguousConstructors = null;
         }
         else if (constructorToUse != null && typeDiffWeight == minTypeDiffWeight) {
            if (ambiguousConstructors == null) {
               ambiguousConstructors = new LinkedHashSet<>();
               ambiguousConstructors.add(constructorToUse);
            }
            ambiguousConstructors.add(candidate);
         }
      }

      if (constructorToUse == null) {
         if (causes != null) {
            UnsatisfiedDependencyException ex = causes.removeLast();
            for (Exception cause : causes) {
               this.beanFactory.onSuppressedException(cause);
            }
            throw ex;
         }
         throw new BeanCreationException(mbd.getResourceDescription(), beanName,
               "Could not resolve matching constructor " +
               "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities)");
      }
      else if (ambiguousConstructors != null && !mbd.isLenientConstructorResolution()) {
         throw new BeanCreationException(mbd.getResourceDescription(), beanName,
               "Ambiguous constructor matches found in bean '" + beanName + "' " +
               "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " +
               ambiguousConstructors);
      }

      if (explicitArgs == null) {
         argsHolderToUse.storeCache(mbd, constructorToUse);
      }
   }
//根据实例化策略以及得到的构造函数、参数类型 实例化bean
   bw.setBeanInstance(instantiate(beanName, mbd, constructorToUse, argsToUse));
   return bw;
}
```