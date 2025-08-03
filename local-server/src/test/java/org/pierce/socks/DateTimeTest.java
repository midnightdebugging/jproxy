package org.pierce.socks;

import org.junit.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTest {
    @Test
    public void test001() {
        // 当前日期时间（带时区偏移）
        System.out.println("带时区偏移: " + OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        // UTC 时间
        System.out.println("UTC 时间: " + Instant.now().toString());

        DateTimeFormatter full = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        System.out.println("含毫秒: " + OffsetDateTime.now().format(full));
    }
}
