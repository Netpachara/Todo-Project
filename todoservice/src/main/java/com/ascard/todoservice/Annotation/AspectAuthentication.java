package com.ascard.todoservice.Annotation;


import com.ascard.todoservice.Exception.TokenException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(2)
public class AspectAuthentication {

    private HttpServletRequest request ;

    public AspectAuthentication(HttpServletRequest hs){
        this.request = hs;
    }

    @Before("@annotation(com.ascard.todoservice.Annotation.BypassAuthentication)")
//    @Before("execution(* com.ascard.todoservice.controller.NewController.checkBypass(..))")
    public void BeforeBypass() throws Throwable {
        System.out.println("Bypass Before");
        String getReq = request.getHeader("Net-Bypass");
        if(getReq == null || !getReq.equalsIgnoreCase("realBypass")){
            throw new TokenException("invalid", "byPassToken is invalid");
        }
    }
}
