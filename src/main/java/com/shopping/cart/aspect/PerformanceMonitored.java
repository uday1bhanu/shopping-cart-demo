package com.shopping.cart.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to monitor method performance and throw exceptions for slow operations.
 * Can be applied to any method to track execution time.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceMonitored {

    /**
     * Maximum allowed execution time in milliseconds.
     * Default is 3000ms (3 seconds).
     */
    long thresholdMs() default 3000;

    /**
     * Custom error message to use when threshold is exceeded.
     * If empty, a default message will be generated.
     */
    String errorMessage() default "";
}
