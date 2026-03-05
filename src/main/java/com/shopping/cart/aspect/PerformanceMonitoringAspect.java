package com.shopping.cart.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect for monitoring method performance across the application.
 * Intercepts methods annotated with @PerformanceMonitored and tracks execution time.
 * Throws RuntimeException if execution exceeds configured threshold.
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitoringAspect {

    @Around("@annotation(com.shopping.cart.aspect.PerformanceMonitored)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PerformanceMonitored annotation = method.getAnnotation(PerformanceMonitored.class);

        String methodName = signature.getDeclaringType().getSimpleName() + "." + method.getName();
        long startTime = System.currentTimeMillis();

        try {
            // Execute the actual method
            Object result = joinPoint.proceed();

            // Check execution time after successful completion
            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > annotation.thresholdMs()) {
                String errorMessage = annotation.errorMessage().isEmpty()
                    ? "Unable to load the recommendations!"
                    : annotation.errorMessage();

                log.error("{} took {} ms - exceeds threshold of {} ms",
                         methodName, executionTime, annotation.thresholdMs());

                throw new RuntimeException(errorMessage);
            }

            log.info("{} completed in {} ms", methodName, executionTime);
            return result;

        } catch (RuntimeException e) {
            // Re-throw if it's our performance exception or any other runtime exception
            throw e;
        } catch (Throwable t) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("{} failed after {} ms", methodName, executionTime, t);
            throw t;
        }
    }
}
