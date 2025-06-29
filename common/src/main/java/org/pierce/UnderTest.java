package org.pierce;

import java.lang.annotation.*;

/**
 * 标识该类/方法尚未经过充分测试，可能存在未发现的缺陷
 */
@Documented
@Retention(RetentionPolicy.SOURCE) // 仅保留在源码中
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface UnderTest {
    String value() default ""; // 可添加备注
}
