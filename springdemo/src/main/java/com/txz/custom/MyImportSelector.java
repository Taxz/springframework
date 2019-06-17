package com.txz.custom;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * description:
 *
 * @author Taxz
 * @create 2019-06-14 13:51
 */
public class MyImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        for (String str : annotationMetadata.getAnnotationTypes()) {
            System.out.println("注解信息："+str);
        }
        return new String[]{"com.txz.controll.MokeyControll"};
    }
}
