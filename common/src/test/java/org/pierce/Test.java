package org.pierce;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.pierce.codec.SocksCommandConnectRequest;

import java.nio.charset.StandardCharsets;

public class Test {
    //
    public static void main(String[] args) {
        SocksCommandConnectRequest socksCommandConnectRequest = new SocksCommandConnectRequest();
        socksCommandConnectRequest.setTarget("192.168.31.129");
        socksCommandConnectRequest.setPort(2080);
        ByteBuf bf = Unpooled.buffer();
        String clazzName = socksCommandConnectRequest.getClass().getName();
        try {
            bf.writeInt(clazzName.length());
            bf.writeBytes(clazzName.getBytes());
            String json = UtilTools.objToString(socksCommandConnectRequest);
            bf.writeInt(json.length());
            bf.writeBytes(json.getBytes());

            //Deserialization
            int length1 = bf.readInt();
            byte[] newClazzName = new byte[length1];
            bf.readBytes(newClazzName);
            int length2 = bf.readInt();
            byte[] newJson = new byte[length2];
            bf.readBytes(newJson);
            System.out.printf("length1:%d\n", length1);
            System.out.printf("newClazzName:%s\n", new String(newClazzName));
            System.out.printf("length2:%d\n", length2);
            System.out.printf("newJson:%s\n", new String(newJson));
            Class<?> clazz = Class.forName(new String(newClazzName));
            String newJsonStr = new String(newJson, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            SocksCommandConnectRequest newObj = (SocksCommandConnectRequest) gson.fromJson(newJsonStr, clazz);
            System.out.printf("newObj.getTarget():%s\n", newObj.getTarget());
            System.out.printf("newObj.getPort():%d\n", newObj.getPort());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            bf.release();
        }
    }
}
