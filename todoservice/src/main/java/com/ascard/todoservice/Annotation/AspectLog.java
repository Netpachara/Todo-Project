package com.ascard.todoservice.Annotation;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Order(1)
public class AspectLog {

    @Before("@annotation(com.ascard.todoservice.Annotation.NetLog)")
//    @Before("execution(* com.ascard.todoservice.controller.NewController.checkBypass(..))")
    public void BeforeAop(){
        System.out.println("NetLog Before");
    }

    @After("@annotation(com.ascard.todoservice.Annotation.NetLog)")
//    @After("execution(* com.ascard.todoservice.controller.NewController.checkBypass(..))")
    public void AfterAop(){
        System.out.println("NetLog After");
    }

}
