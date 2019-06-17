package com.txz.config;

import org.springframework.core.io.Resource;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;

/**
 * description:
 *
 * @author Taxz
 * @create 2019-06-14 11:09
 */
public class MyTypeFilter implements TypeFilter {
    /**
     *
     * @param metadataReader 读取到的当前正在扫描的类的信息
     * @param metadataReaderFactory 可以获取到其他任何类信息的
     */
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {

        //获取当前注解的信息
        ClassMetadata annotation = metadataReader.getAnnotationMetadata();
        //获取当前的类信息
        ClassMetadata zlass = metadataReader.getClassMetadata();
        //获取当前类的资源(类路径..)
        Resource resource = metadataReader.getResource();

        String className = zlass.getClassName();
        //System.out.println("className:"+className);
        if (className.contains("cat")) {
            return true;
        }
        return false;
    }
}
