package com.txz.custom;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * description:
 * 自定义
 *
 * @author Taxz
 * @create 2019-06-24 11:00
 */

@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        System.out.println("MyBeanFactoryPostProcessor 在所有的bean定义加载完成后,bean实例化之前调用");
        int count = configurableListableBeanFactory.getBeanDefinitionCount();
        System.out.println("spring中共有bena:"+count);
        String[] names = configurableListableBeanFactory.getBeanDefinitionNames();
        System.out.println(String.join(",", names));
    }
}
