/*package com.ebayinventory.ebay;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoggingAspect {

	@AfterThrowing(pointcut = "execution(* com.ebayinventory.ebay.EbayFacade.*(..))", throwing = "error")
	public void logBefore(JoinPoint joinPoint, Throwable error) {
		Throwable cause = error.getCause();
		System.out.println("hijacked : " + joinPoint.getSignature().getName());
	}

}*/