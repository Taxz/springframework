package com.txz.config;

import com.txz.bean.Mokey;
import com.txz.service.MokeyService;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * description:
 * 注解扫描
 *
 * @author Taxz
 * @create 2019-06-14 10:36
 */
//@ComponentScan  value:指定要扫描的包
//excludeFilters = Filter[] ：指定扫描的时候按照什么规则排除那些组件
//includeFilters = Filter[] ：指定扫描的时候只需要包含哪些组件
//FilterType.ANNOTATION：按照注解
//FilterType.ASSIGNABLE_TYPE：按照给定的类型；
//FilterType.ASPECTJ：使用ASPECTJ表达式
//FilterType.REGEX：使用正则指定
//FilterType.CUSTOM：使用自定义规则

@Configuration//表示为一个配置类 相当于web.xml
@ComponentScans(
        value = {
                @ComponentScan(
                        value = "com.txz", includeFilters = {
                        //@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class})/*,
                        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {MokeyService.class})
                        //,@ComponentScan.Filter(type = FilterType.CUSTOM, classes = {MyTypeFilter.class})
                }, useDefaultFilters = false) //true 表示会自动扫描带有@Component、@Repository、@Service和@Controller的类
        }
)
public class BeanScansConfig {

    @Bean
    public Mokey mokey() {
        return new Mokey("mokey", 10);
    }
}
