package org.pierce;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.pierce.entity.ProtocolInfo;
import org.pierce.session.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilTools {

    final static DateTimeFormatter FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final Logger log = LoggerFactory.getLogger(UtilTools.class);

    public void closeCtx(ChannelHandlerContext ctx) {
        if (ctx == null) {
            return;
        }
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

    public static String objToString(Object obj) {
        Gson gson = new GsonBuilder()
                .serializeNulls()    // 可选：序列化 null 值
                .create();
        return gson.toJson(obj);
    }

    public static String objToString(Object obj, boolean pretty) {
        if (pretty) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting() // 关键设置：启用美化输出
                    .serializeNulls()    // 可选：序列化 null 值
                    .create();
            return gson.toJson(obj);
        } else {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting() // 关键设置：启用美化输出
                    .create();
            return gson.toJson(obj);
        }

    }

    public static String formatChannelInfo(Channel channel) {
        if (!channel.hasAttr(SessionAttributes.TARGET_ADDRESS)) {
            return String.format(
                    "[id: %s, L:%s - R:%s]",
                    channel.id().asShortText(),
                    channel.localAddress(),
                    channel.remoteAddress()
            );
        }
        String targetAddress = channel.attr(SessionAttributes.TARGET_ADDRESS).get();
        Integer targetPort = channel.attr(SessionAttributes.TARGET_PORT).get();
        if (targetPort == null) {
            targetPort = -1;
        }
        return String.format(
                "[id: %s, L:%s - R:%s T:%s:%d]",
                channel.id().asShortText(),
                channel.localAddress(),
                channel.remoteAddress(),
                targetAddress,
                targetPort
        );


    }

    public static String formatChannelInfo(Channel channel0, Channel channel1) {
        return formatChannelInfo(channel0) + "<===>" + formatChannelInfo(channel1);
    }

    public static String formatChannelInfo(ChannelHandlerContext ctx) {
        return formatChannelInfo(ctx.channel());
    }

    public static String formatChannelInfo(ChannelHandlerContext ctx0, ChannelHandlerContext ctx1) {
        return formatChannelInfo(ctx0) + "<===>" + formatChannelInfo(ctx1);
    }

    public static String currentTime() {

        return OffsetDateTime.now().format(FULL);
    }

    private final static Pattern protocolPattern = Pattern.compile("^[a-z]+://");

    private final static Pattern hostPortPattern = Pattern.compile("^(?:([^:/]+)|\\[([^/]+)])(:\\d+)?(/|$)");

    public static ProtocolInfo parseProtocolInfo(String url, boolean ipv6Expand) {
        ProtocolInfo protocolInfo = parseProtocolInfo(url);
        if (!ipv6Expand) {
            return protocolInfo;
        }
        InetAddressValidator ipValidator = InetAddressValidator.getInstance();
        if (!ipValidator.isValidInet6Address(protocolInfo.getHostAddress())) {
            return protocolInfo;
        }
        protocolInfo.setHostAddress(UtilTools.iv6Expander(protocolInfo.getHostAddress()));
        return protocolInfo;
    }

    public static ProtocolInfo parseProtocolInfo(String url) {

        InetAddressValidator ipValidator = InetAddressValidator.getInstance();
        ProtocolInfo protocolInfo = new ProtocolInfo();
        if (ipValidator.isValidInet6Address(url)) {
            protocolInfo.setHostAddress(url);
            return protocolInfo;
        }

        Matcher matcher = protocolPattern.matcher(url);
        if (matcher.find()) {

            protocolInfo.setProtocol(url.substring(matcher.start(), matcher.end()));

            url = url.substring(matcher.end());
        }

        matcher = hostPortPattern.matcher(url);

        if (matcher.find()) {
            String hostAddress = matcher.group(1);
            if (hostAddress == null) {
                hostAddress = matcher.group(2);
            }
            protocolInfo.setHostAddress(hostAddress);
            String portStr = matcher.group(3);
            if (portStr != null) {
                portStr = portStr.substring(1);
                protocolInfo.setPort(Integer.parseInt(portStr));
            } else {
                if ("https://".equals(protocolInfo.getProtocol())) {
                    protocolInfo.setPort(443);
                } else if ("http://".equals(protocolInfo.getProtocol())) {
                    protocolInfo.setPort(80);
                }
            }
            protocolInfo.setPath("/" + url.substring(matcher.end()));
        }
        return protocolInfo;
    }

    public static String iv6Expander(String ipv6) {
        try {
            // 解析IPv6地址
            InetAddress address = InetAddress.getByName(ipv6);
            // 获取展开后的地址（自动填充省略的零）
            String expanded = address.getHostAddress();

            // 分割每个块并格式化为4位十六进制
            String[] blocks = expanded.split(":");
            StringBuilder formatted = new StringBuilder();
            for (String block : blocks) {
                // 每个块补前导零至4位
                String formattedBlock = String.format("%4s", block).replace(' ', '0');
                formatted.append(formattedBlock).append(":");
            }
            // 移除末尾多余的冒号
            formatted.setLength(formatted.length() - 1);
            return formatted.toString();
        } catch (Exception e) {
            log.info("Exception e", e);
            return ipv6;
        }

    }
}
