package com.txz.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

import java.util.Arrays;

/**
 * description:
 * @Aspect 在spring中注册一个切面
 *
 * @author Taxz
 * @create 2019-06-20 15:46
 */
@Aspect
public class SecretaryAspect {

    //抽取公共的切入点表达式
    //1、本类引用
    //2、其他的切面引用
    @Pointcut("execution(public * com.txz.bean.Boss.*(..))")
    public void pointCut() {
    }

    //@Before在目标方法之前切入；切入点表达式（指定在哪个方法切入）
    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        System.out.println("小秘:" + joinPoint.getSignature().getName() + " {" + Arrays.asList(args) + "}");
    }

    @After("pointCut()")
    public void after(JoinPoint joinPoint) {
        System.out.println("小秘:好的boss");
    }

    //JoinPoint一定要出现在参数表的第一位
    @AfterReturning(value = "pointCut()", returning = "result")
    public void returnComm(JoinPoint joinPoint, Object result) {
        System.out.println("小秘:转boss的话," + joinPoint.getSignature().getName() + " {" + result + "}");
    }

    @AfterThrowing(value = "pointCut()",throwing = "exception")
    public void exception(JoinPoint joinPoint,Exception exception) {
        System.out.println("小秘:boss 发火了"+exception);
    }


}
