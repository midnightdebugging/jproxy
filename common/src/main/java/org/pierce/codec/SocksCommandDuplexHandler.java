package org.pierce.codec;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.pierce.UtilTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class SocksCommandDuplexHandler extends ChannelDuplexHandler {

    final static Logger log = LoggerFactory.getLogger(SocksCommandDuplexHandler.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            ctx.fireChannelRead(toSocksCommand((BinaryWebSocketFrame) msg));
            return;
        }
        if (msg instanceof SocksCommand) {
            ctx.fireChannelRead(toBinaryWebSocketFrame((SocksCommand) msg));
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof BinaryWebSocketFrame) {
            ctx.write(toSocksCommand((BinaryWebSocketFrame) msg), promise);
            return;
        }
        if (msg instanceof SocksCommand) {
            ctx.write(toBinaryWebSocketFrame((SocksCommand) msg), promise);
            return;
        }
        super.write(ctx, msg, promise);
    }

    private SocksCommand toSocksCommand(BinaryWebSocketFrame binaryWebSocketFrame) throws ClassNotFoundException {
        ByteBuf byteBuf = binaryWebSocketFrame.content().copy();
        try {
            log.debug("{}", ByteBufUtil.prettyHexDump(byteBuf));

            int length1 = byteBuf.readInt();
            byte[] newClazzName = new byte[length1];
            byteBuf.readBytes(newClazzName);
            int length2 = byteBuf.readInt();
            byte[] newJson = new byte[length2];
            byteBuf.readBytes(newJson);
            Class<?> clazz = Class.forName(new String(newClazzName));
            String newJsonStr = new String(newJson, StandardCharsets.UTF_8);

            Gson gson = new Gson();

            return (SocksCommand) gson.fromJson(newJsonStr, clazz);
        } finally {
            byteBuf.release();
        }
    }

    private BinaryWebSocketFrame toBinaryWebSocketFrame(SocksCommand socksCommand) {
        ByteBuf byteBuf = Unpooled.buffer();
        String clazzName = socksCommand.getClass().getName();
        try {
            byteBuf.writeInt(clazzName.length());
            byteBuf.writeBytes(clazzName.getBytes());
            String json = UtilTools.objToString(socksCommand);
            byteBuf.writeInt(json.length());
            byteBuf.writeBytes(json.getBytes());
            log.info("{}", ByteBufUtil.prettyHexDump(byteBuf));
            return new BinaryWebSocketFrame(byteBuf.retain());
        } finally {

            byteBuf.release();
        }
    }
}


