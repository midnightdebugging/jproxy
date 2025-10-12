package org.pierce.manage.handler.entity;

import org.pierce.UtilTools;

import java.nio.charset.StandardCharsets;

public class DefaultReturn {

    int httpCode;

    Object message;

    public DefaultReturn(Object message) {
        this.httpCode = 200;
        this.message = message;
    }

    public DefaultReturn(int httpCode, Object message) {
        this.httpCode = httpCode;
        this.message = message;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public String toString() {
        return UtilTools.objToString(this);
    }

    public static DefaultReturn newInstance(Object message) {
        return new DefaultReturn(message);
    }

    public static DefaultReturn newInstance(int httpCode, Object message) {
        return new DefaultReturn(httpCode, message);
    }

    public static byte[] newInstanceBytes(Object message) {
        return new DefaultReturn(message).toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] newInstanceBytes(int httpCode, Object message) {
        return new DefaultReturn(httpCode, message).toString().getBytes(StandardCharsets.UTF_8);
    }
}
