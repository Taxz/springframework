package com.txz.custom;

import com.txz.dao.MokeyDao;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * description:
 *
 * @author Taxz
 * @create 2019-06-14 13:56
 */
public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    //它允许我们直接通过BeanDefinitionRegistry对象注册bean。
    /**
     * AnnotationMetadata：当前类的注解信息
     * BeanDefinitionRegistry:BeanDefinition注册类；
     * 		把所有需要添加到容器中的bean；调用
     * 		BeanDefinitionRegistry.registerBeanDefinition手工注册进来
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        boolean service = beanDefinitionRegistry.containsBeanDefinition("com.txz.service.MokeyService");
        boolean controll = beanDefinitionRegistry.containsBeanDefinition("com.txz.controll.MokeyControll");
        if (service && controll) {
            RootBeanDefinition beanDef = new RootBeanDefinition(MokeyDao.class);
            beanDefinitionRegistry.registerBeanDefinition("mkd",beanDef);
        }
    }
}
