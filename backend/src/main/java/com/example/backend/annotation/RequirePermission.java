package com.example.backend.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * 需要的权限码
     */
    String[] value();

    /**
     * 权限逻辑：AND-需要全部权限，OR-需要任一权限
     */
    Logical logical() default Logical.OR;

    enum Logical {
        AND, OR
    }
}
