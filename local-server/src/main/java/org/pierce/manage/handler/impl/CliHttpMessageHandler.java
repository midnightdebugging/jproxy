package org.pierce.manage.handler.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.apache.commons.cli.*;
import org.apache.ibatis.session.SqlSession;
import org.pierce.DataBase;
import org.pierce.list.Directive;
import org.pierce.list.MatchType;
import org.pierce.list.entity.NameEntity;
import org.pierce.list.imp.DataBaseNameListCheck;
import org.pierce.list.imp.GFWNameListCheck;
import org.pierce.list.mapper.NameListMapper;
import org.pierce.manage.handler.HttpMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CliHttpMessageHandler implements HttpMessageHandler {


    final static Logger log = LoggerFactory.getLogger(CliHttpMessageHandler.class);

    @Override
    public FullHttpResponse handle(FullHttpRequest request) throws IOException {
        ByteBuf output = Unpooled.buffer();
        ByteBuf input = request.content().retain();
        try {

            byte[] bytes = ByteBufUtil.getBytes(input);


            String body = new String(bytes);
            //log.info("0-body:{}", body);
            body = URLDecoder.decode(body, StandardCharsets.UTF_8);
            //log.info("1-body:{}", body);


            String[] args = parser(body);

            String returnMsg = process(args);

            output.writeBytes(returnMsg.getBytes());


            FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK,
                    output);
            response.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            return response;


        } catch (Exception e) {
            output.release();
            throw e;
        } finally {
            input.release();
        }
    }

    private String process(String[] args) {
        try {
            if (args == null || args.length == 0) {
                return "usage [db-list|gfw-list]\n ";
            }
            if ("db-list".equals(args[0])) {
                return dbList(args);
            }
            if ("gfw-list".equals(args[0])) {
                return gfwList(args);
            }
            return "usage [db-list|gfw-list]\n ";
        } catch (Throwable e) {
            log.info("Throwable e", e);
            return e.getLocalizedMessage();
        }
    }

    public String getDbListHelper(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        // 创建 StringWriter 来捕获输出
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        // 在 header 中描述非选项参数
        String header = """
                非选项参数:
                  URL    要增加规则的URL
                
                选项:""";

        final String footer = """
                
                示例:
                  db-list -r -3 ".*\\.google.com$"
                
                """;

        formatter.printHelp(
                printWriter,           // 输出到 PrintWriter
                HelpFormatter.DEFAULT_WIDTH,  // 行宽度
                "db-list URL",               // 程序名称
                header,         // 抬头描述
                options,               // Options 对象
                HelpFormatter.DEFAULT_LEFT_PAD,    // 左缩进
                HelpFormatter.DEFAULT_DESC_PAD,    // 描述缩进
                footer,            // 页脚
                true                  // 是否自动使用Usage
        );
        printWriter.flush();
        return stringWriter.toString();

    }

    private String dbList(String[] args) throws ParseException {


        Options options = new Options();

        options.addOption("h", "help", false, "显示帮助信息");
        options.addOption("v", "verbose", false, "详细输出模式");


        {
            OptionGroup mutuallyExclusiveGroup = new OptionGroup();
            Option option = Option.builder("r")
                    .longOpt("regular")
                    .desc("匹配方式：正则表达式匹配(默认)")
                    .build();
            mutuallyExclusiveGroup.addOption(option);

            option = Option.builder("s")
                    .longOpt("subdomain")
                    .desc("匹配方式：所有子域名")
                    .build();
            mutuallyExclusiveGroup.addOption(option);

            option = Option.builder("e")
                    .longOpt("equals")
                    .desc("匹配方式：相等")
                    .build();
            mutuallyExclusiveGroup.addOption(option);

            options.addOptionGroup(mutuallyExclusiveGroup);

        }

        {
            OptionGroup mutuallyExclusiveGroup = new OptionGroup();
            Option option = Option.builder("0")
                    .longOpt("ban")
                    .desc("收到请求后：禁止链接")
                    .build();
            mutuallyExclusiveGroup.addOption(option);

            option = Option.builder("1")
                    .longOpt("local")
                    .desc("收到请求后：本地直接链出，不经过remote-server")
                    .build();
            mutuallyExclusiveGroup.addOption(option);

            option = Option.builder("2")
                    .longOpt("half")
                    .desc("收到请求后：先发请求给remote-server进行域名解析，然后再确定使用什么方式进行链接")
                    .build();
            mutuallyExclusiveGroup.addOption(option);

            option = Option.builder("3")
                    .longOpt("full")
                    .desc("收到请求后：全部流量经过remote-server")
                    .build();
            mutuallyExclusiveGroup.addOption(option);

            options.addOptionGroup(mutuallyExclusiveGroup);

        }

        options.addOption(Option.builder("R")
                .longOpt("reload")
                .argName("匹配类型")
                .desc("重新加载")
                .build());

        options.addOption(Option.builder("l")
                .longOpt("list")
                .desc("列举")
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String[] remainingArgs = cmd.getArgs();

        if (cmd.hasOption("help")) {
            return getDbListHelper(options);
        }


        StringBuilder stringBuilder = new StringBuilder();


        if (remainingArgs.length == 2) {
            Directive directive;
            MatchType matchType;

            if (cmd.hasOption("subdomain")) {
                matchType = MatchType.SUBNET;
            } else if (cmd.hasOption("equals")) {
                matchType = MatchType.EQUAL;
            } else {
                matchType = MatchType.REGULAR_MATCHING;
            }
            if (cmd.hasOption("ban")) {
                directive = Directive.DIRECT_CONNECT;
            } else if (cmd.hasOption("local")) {
                directive = Directive.DIRECT_CONNECT;
            } else if (cmd.hasOption("half")) {
                directive = Directive.DOMAIN_NAME_QUERY_FIRST;
            } else {
                directive = Directive.FULL_CONNECT;
            }


            NameEntity entity = new NameEntity();
            entity.setLabel("default");
            entity.setDirective(String.valueOf(directive));
            entity.setData(remainingArgs[1]);
            entity.setMatchType(String.valueOf(matchType));
            try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {
                NameListMapper mapper = sqlSession.getMapper(NameListMapper.class);
                int changed = mapper.insert(entity);
                log.info("changed:{}", changed);
                stringBuilder.append(String.format("changed:%d\n", changed));
                sqlSession.commit();
            }
        }

        if (cmd.hasOption("reload")) {
            DataBaseNameListCheck.getInstance().reload();
            stringBuilder.append("reloaded\n");
        }
        if (cmd.hasOption("list")) {
            stringBuilder.append(DataBaseNameListCheck.getInstance().list());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public String getGfwListHelper(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        // 创建 StringWriter 来捕获输出
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        // 在 header 中描述非选项参数

        final String footer = """
                
                示例:
                  gfw-list -d -R -l"
                
                """;

        formatter.printHelp(
                printWriter,           // 输出到 PrintWriter
                HelpFormatter.DEFAULT_WIDTH,  // 行宽度
                "gfw-list",               // 程序名称
                "-----------------------",         // 抬头描述
                options,               // Options 对象
                HelpFormatter.DEFAULT_LEFT_PAD,    // 左缩进
                HelpFormatter.DEFAULT_DESC_PAD,    // 描述缩进
                footer,            // 页脚
                true                  // 是否自动使用Usage
        );
        printWriter.flush();
        return stringWriter.toString();

    }

    private String gfwList(String[] args) throws ParseException, InterruptedException, IOException {


        Options options = new Options();

        options.addOption("h", "help", false, "显示帮助信息");
        options.addOption("v", "verbose", false, "详细输出模式");
        //options.addOption("d", "download", false, "下载gfwlist");

        Option debugOption = Option.builder("d")
                .longOpt("download")
                .desc("下载gfwlist(可指定地址)")
                .hasArg(true)
                .optionalArg(true)
                .build();
        options.addOption(debugOption);

        options.addOption("R", "reload", false, "重新加载");
        options.addOption("l", "list", false, "列举");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            return getGfwListHelper(options);
        }


        StringBuilder stringBuilder = new StringBuilder();

        if (cmd.hasOption("download")) {
            String download = cmd.getOptionValue("download");
            if (download == null || download.isEmpty()) {
                GFWNameListCheck.getInstance().download();
            } else {
                GFWNameListCheck.getInstance().download(download);
            }
            stringBuilder.append(String.format("download:%s", download)).append("\n");
        }


        if (cmd.hasOption("reload")) {
            stringBuilder.append("reload").append("\n");
            GFWNameListCheck.getInstance().reload();
        }
        if (cmd.hasOption("list")) {
            stringBuilder.append("list").append("\n");
            stringBuilder.append(GFWNameListCheck.getInstance().list());
        }
        return stringBuilder.toString();
    }

    public String[] parser(String cmdLine) {

        List<String> list = new ArrayList<>();
        /*
        0-not in string<br>
        1- in string
         */
        int flag = 0;
        //打开单引号\\双引号
        int quotation = 0XFFFF;
        int escapeByte = '\\';
        boolean escape = false;


        byte[] bytes = cmdLine.getBytes(StandardCharsets.UTF_8);
        try (ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
            byte aByte;
            for (byte b : bytes) {
                aByte = b;
                //System.out.printf("0==%c %d %s %x\n", (char) aByte, flag, escape, quotation);
                //System.out.printf("escape:%s\n", escape);

                if (escape) {
                    escape = false;
                    bao.write(aByte);
                    continue;
                } else if (aByte == escapeByte) {
                    escape = true;
                    flag = 1;
                    continue;
                }

                //只有开始的时候才能设置flag
                if (flag == 0) {
                    if (isBlank(aByte)) {
                        continue;
                    }
                    flag = 1;
                    if (isQuotation(aByte)) {
                        //标注打开了引号
                        quotation = aByte;
                        continue;
                    }
                    bao.write(aByte);
                    continue;

                }
                if (flag == 1) {
                    //设置引号打开时先检查引号
                    if (quotation != 0XFFFF) {
                        if (aByte == quotation) {
                            //清理引号标记
                            quotation = 0XFFFF;
                            flag = 0;
                            String tmpStr = bao.toString(StandardCharsets.UTF_8);
                            if (!tmpStr.trim().isEmpty()) {
                                list.add(tmpStr.trim());
                            }
                            bao.reset();
                            continue;
                        }
                    } else {
                        //没设置引号时，以空格分界
                        if (isBlank(aByte)) {
                            flag = 0;
                            String tmpStr = bao.toString(StandardCharsets.UTF_8);
                            if (!tmpStr.trim().isEmpty()) {
                                list.add(tmpStr.trim());
                            }
                            bao.reset();
                            continue;
                        }
                    }

                    bao.write(aByte);
                    continue;
                }

            }
            String tmpStr = bao.toString(StandardCharsets.UTF_8);
            if (!tmpStr.trim().isEmpty()) {
                list.add(tmpStr.trim());
            }
            String[] strings = new String[list.size()];
            list.toArray(strings);
            return strings;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean isBlank(int aByte) {
        return aByte == '\t' || aByte == ' ' || aByte == '\n' || aByte == '\r';
    }

    public boolean isQuotation(int aByte) {
        return aByte == '\'' || aByte == '"';
    }
}
