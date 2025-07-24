package org.pierce.nlist.gfw;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;


public class Base64InputStream extends InputStream {

    InputStream dataSource;

    int readIndex = 4;

    byte[] buffer = new byte[4];

    public Base64InputStream() {
    }

    public Base64InputStream(InputStream dataSource) {
        this.dataSource = dataSource;
    }

    public Base64InputStream(String dataSource) {
        this.dataSource = new ByteArrayInputStream(dataSource.getBytes());
    }

    @Override
    public int read() throws IOException {
        if (readIndex < buffer.length) {
            return buffer[readIndex++] & 0xFF;
        }
        //read 4 bytes
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; ) {
            int read = dataSource.read();
            if (read == -1) {
                return -1;
            }
            if (read == '\r' || read == '\n') {
                continue;
            }
            bytes[i++] = (byte) read;
        }
        buffer = Base64.getDecoder().decode(bytes);
        readIndex = 0;
        return buffer[readIndex++] & 0xFF;
    }

    @Override
    public void close() throws IOException {
        dataSource.close();
    }
}
