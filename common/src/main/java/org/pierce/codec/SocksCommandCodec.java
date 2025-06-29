package org.pierce.codec;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.pierce.UtilTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * first message type is a byte, is socksCommand<br>
 * 0x1=SocksCommandConnectRequest<br>
 * 0x2=SocksCommandConnectResponse<br>
 * 0x3=SocksCommandDNSRequest<br>
 * 0x4=SocksCommandDNSResponse<br>
 * next message type is int, is String bytes length<br>
 * next message type is bytes,is a String<br>
 */
public class SocksCommandCodec extends ByteToMessageCodec<SocksCommand> {

    final static List<Class<?>> socksCommandList = new ArrayList<>();

    final static Logger log = LoggerFactory.getLogger(SocksCommandCodec.class);

    static {
        socksCommandList.add(SocksCommandConnectRequest.class);
        socksCommandList.add(SocksCommandConnectResponse.class);
        socksCommandList.add(SocksCommandDNSRequest.class);
        socksCommandList.add(SocksCommandDNSResponse.class);
        //socksCommandList.add(SocksCommandClose.class);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, SocksCommand socksCommand, ByteBuf byteBuf) throws Exception {
        int type = 0x0;
        for (int i = 0; i < socksCommandList.size(); i++) {
            if (socksCommand.getClass().equals(socksCommandList.get(i))) {
                type = i + 1;
                break;
            }
        }
        if (type == 0x0) {
            throw new RuntimeException("非法的类型：" + Integer.toHexString(type));
        }
        Gson gson = new Gson();
        String json = gson.toJson(socksCommand);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        log.info("{} {}##{}", UtilTools.formatChannelInfo(channelHandlerContext), Integer.toHexString(type), UtilTools.objToString(socksCommand));
        byteBuf.writeByte(type);
        /*if (JproxyProperties.booleanVal("debug")) {
            Thread.sleep(500);
            channelHandlerContext.flush();
        }*/
        byteBuf.writeInt(jsonBytes.length);
       /* if (JproxyProperties.booleanVal("debug")) {
            Thread.sleep(500);
            channelHandlerContext.flush();
        }*/
        byteBuf.writeBytes(jsonBytes);
        /*if (JproxyProperties.booleanVal("debug")) {
            Thread.sleep(500);
            channelHandlerContext.flush();
        }*/
        channelHandlerContext.write(jsonBytes);
        channelHandlerContext.flush();
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        // 1. 检查数据长度是否足够（例如：消息头4字节）
        if (byteBuf.readableBytes() < 5) {
            return; // 等待更多数据
        }

        // 2. 标记读指针位置（用于回滚）
        byteBuf.markReaderIndex();


        int type = byteBuf.readByte();
        Class<?> clazz = null;
        if (type == 0x0) {
            byteBuf.resetReaderIndex();
            throw new RuntimeException("非法的类型：" + Integer.toHexString(type));
        }
        if (type > socksCommandList.size()) {
            throw new RuntimeException("非法的类型：" + Integer.toHexString(type));
        }
        clazz = socksCommandList.get(type - 1);
        if (clazz == null) {
            byteBuf.resetReaderIndex();
            throw new RuntimeException("非法的类型：clazz==null");
        }
        int strLength = byteBuf.readInt();

        if (byteBuf.readableBytes() < strLength) {
            byteBuf.resetReaderIndex();
        }

        byte[] jsonBytes = new byte[strLength];
        byteBuf.readBytes(jsonBytes);

        String json = new String(jsonBytes, StandardCharsets.UTF_8);

        Gson gson = new Gson();
        SocksCommand socksCommand = (SocksCommand) gson.fromJson(json, clazz);
        log.info("{} {}##{}", UtilTools.formatChannelInfo(channelHandlerContext), Integer.toHexString(type), UtilTools.objToString(socksCommand));
        list.add(socksCommand);

    }
}
