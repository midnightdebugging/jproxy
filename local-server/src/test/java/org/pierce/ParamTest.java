package org.pierce;

import org.pierce.manage.handler.impl.CliHttpMessageHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ParamTest {
    public static void main(String[] args) throws IOException {
        try (InputStream is = ParamTest.class.getResourceAsStream("/test.str"); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (is == null) {
                return;
            }
            while (true) {

                int b = is.read();
                if (b < 0) {
                    break;
                }
                System.out.printf("read %c\n", (char) b);
                outputStream.write(b);
            }

            String str = outputStream.toString(StandardCharsets.UTF_8);
            System.out.println(str);
            CliHttpMessageHandler api=new CliHttpMessageHandler();
            System.out.printf("%s ==> %s\n", str, api.parser(str));
        }
    }
}
