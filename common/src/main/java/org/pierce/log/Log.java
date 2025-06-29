package org.pierce.log;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    public void info(Object instance, ChannelHandlerContext ctx, String msg, Throwable throwable) {


        StringBuilder sb = new StringBuilder();
        if (instance != null) {
            sb.append(instance.getClass().getName());

        } else {
            sb.append("N/A");
        }
        sb.append("##");

        if (ctx != null) {
            sb.append(ctx.channel().id().asLongText());
        } else {
            sb.append("N/A");
        }

        final Logger logger = LoggerFactory.getLogger(sb.toString());
        sb = new StringBuilder();
        sb.append("##");

        if (msg != null) {
            sb.append(msg);
        }
        sb.append("##");


    }

    public static void main(String[] args) {
        String str = "abc";
        System.out.println(str.getClass().getName());
    }
}
