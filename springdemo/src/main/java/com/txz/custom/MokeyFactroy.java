package com.txz.custom;

import com.txz.bean.Mokey;
import org.springframework.beans.factory.FactoryBean;

/**
 * description:
 *
 * @author Taxz
 * @create 2019-06-14 15:43
 */
public class MokeyFactroy implements FactoryBean<Mokey> {

    //返回Mokey对象 并添加到容器中
    @Override
    public Mokey getObject() throws Exception {
        System.out.println("MokeyFactroy..... 创建mokey实例");
        return new Mokey("mkFactroy",22);
    }

    @Override
    public Class<?> getObjectType() {
        return Mokey.class;
    }

    //是单例？
    //true：这个bean是单实例，在容器中保存一份
    //false：多实例，每次获取都会创建一个新的bean；
    @Override
    public boolean isSingleton() {
        return false;
    }
}
