package org.pierce.session;

import io.netty.util.AttributeKey;

public class SessionAttributes {

    // 声明全局唯一的属性键（推荐使用字符串常量）
    public static final AttributeKey<String> TARGET_ADDRESS = AttributeKey.valueOf("targetAddress");
    public static final AttributeKey<Integer> TARGET_PORT = AttributeKey.valueOf("targetPort");

    public static final AttributeKey<String> OVER_ADDRESS = AttributeKey.valueOf("overAddress");
    public static final AttributeKey<Integer> OVER_PORT = AttributeKey.valueOf("overPort");

    public static final AttributeKey<String> REQUEST_METHOD = AttributeKey.valueOf("requestMethod");

    public static final AttributeKey<String> WORK_TYPE = AttributeKey.valueOf("workType");
}