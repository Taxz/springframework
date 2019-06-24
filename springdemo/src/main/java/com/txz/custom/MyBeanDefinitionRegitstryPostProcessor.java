package com.txz.custom;

import com.txz.bean.Boss;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * description:
 * 自定义扩展类
 *
 * @author Taxz
 * @create 2019-06-24 11:16
 */

@Component
public class MyBeanDefinitionRegitstryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        System.out.println("MyBeanDefinitionRegitstryPostProcessor 获取到bean数量:" + beanDefinitionRegistry.getBeanDefinitionCount());
        RootBeanDefinition root = new RootBeanDefinition();
        root.setBeanClass(Boss.class);
        System.out.println("追加bean定义");
        beanDefinitionRegistry.registerBeanDefinition("wa", root);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        System.out.println("MyBeanDefinitionRegitstryPostProcessor --> postProcessBeanFactory  获取到bean数量:" + configurableListableBeanFactory.getBeanDefinitionCount());

    }
}
