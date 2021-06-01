package com.th.common.utils;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented //标记注解
public @interface  Cache {

    /**
     * 缓存时间，默认为60秒
     * @return
     */
    String time() default "60";
}
