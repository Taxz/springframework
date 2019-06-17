package com.txz.bean;

import org.springframework.beans.factory.annotation.Value;

/**
 * description:
 *
 * @author Taxz
 * @create 2019-06-14 10:55
 */
public class Mokey {

    @Value("小k")
    private String name;
    @Value("#{20-1}")
    private int age;

    @Value("${person.nickName}")
    private String nickName;

    public Mokey() {
    }

    public Mokey(String name, int age) {
        System.out.println("实例化");
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
