package com.txz.custom;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by admin on 2019/6/27.
 */

@Component
public class MyApplicationListener implements ApplicationListener<ApplicationEvent> {
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        System.out.println("接收到事件:"+applicationEvent);
    }
}
