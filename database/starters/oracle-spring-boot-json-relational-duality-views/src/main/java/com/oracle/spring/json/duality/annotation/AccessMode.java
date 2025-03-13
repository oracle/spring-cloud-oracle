package com.oracle.spring.json.duality.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessMode {
    boolean insert() default false;
    boolean update() default false;
    boolean delete() default false;
}
