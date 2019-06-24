package com.txz.config;

import com.txz.bean.cat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * description:
 * spring扩展类配置
 *
 * @author Taxz
 * @create 2019-06-24 10:58
 */
@ComponentScan("com.txz.custom")
@Configuration
public class ExtConfig {

    @Bean
    public cat cat() {
        return new cat();
    }


}
