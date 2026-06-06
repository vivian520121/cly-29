package com.cly.project.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    String module() default "";

    String operation() default "";

    String businessType() default "";

    int businessIdIndex() default -1;

    boolean saveRequest() default true;

    boolean saveResponse() default true;
}
