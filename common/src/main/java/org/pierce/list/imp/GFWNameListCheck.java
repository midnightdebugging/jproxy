package org.pierce.list.imp;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.pierce.Downloader;
import org.pierce.JproxyProperties;
import org.pierce.UtilTools;
import org.pierce.entity.ProtocolInfo;
import org.pierce.imp.HttpDownloader;
import org.pierce.list.Directive;
import org.pierce.list.NameListCheck;
import org.pierce.list.gfw.Base64InputStream;
import org.pierce.list.gfw.GFWDirective;
import org.pierce.list.gfw.GFWRuleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GFWNameListCheck extends DefaultNameListCheck implements NameListCheck {

    private static final Logger log = LoggerFactory.getLogger(GFWNameListCheck.class);

    ArrayList<GFWRuleEntity> gfwRuleEntities = new ArrayList<GFWRuleEntity>();

    private static GFWNameListCheck instance = new GFWNameListCheck() {
        {
            try {
                loadConfigure();
            } catch (IOException e) {
                log.error("loadConfigure,error", e);
                throw new RuntimeException(e);
            }
        }
    };

    public static GFWNameListCheck getInstance() {
        if (instance == null) {
            instance = new GFWNameListCheck();
        }
        return instance;
    }


    public GFWNameListCheck() {
    }


    public GFWNameListCheck(InputStream is) throws IOException {
        parser(is);
    }

    public void loadConfigure() throws IOException {
        loadConfigure(false);
    }

    public void loadConfigure(boolean needSucceed) throws IOException {
        String listPath = JproxyProperties.getProperty("gfw-path");
        File file = new File(listPath);
        if (!file.isFile()) {
            if (needSucceed) {
                throw new RuntimeException("!file.isFile():" + listPath);
            }
            log.error("gfw-path no exist:{}", listPath);
            return;
        }
        parser(Files.newInputStream(file.toPath()));
    }

    public void parser(InputStream is) throws IOException {
        boolean firstLine = true;
        try (Base64InputStream base64InputStream = new Base64InputStream(is)) {
            Scanner scanner = new Scanner(base64InputStream);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                //log.info("line:{}", line);

                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.startsWith("!")) {
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                GFWRuleEntity gfwRuleEntity = parserGFWRuleEntity(line);
                gfwRuleEntities.add(gfwRuleEntity);

            }
        }
        GFWRuleEntity[] gfwRuleEntityArr = new GFWRuleEntity[gfwRuleEntities.size()];
        gfwRuleEntities.toArray(gfwRuleEntityArr);
        Arrays.sort(gfwRuleEntityArr, (o1, o2) -> {
            String score1 = "a";
            String score2 = "a";
            if (o1.isExclude()) {
                score1 = "z";
            }
            if (o2.isExclude()) {
                score2 = "z";
            }
            score1 = score1 + o1.getGfwDirective() + o1.getOriData();
            score2 = score2 + o2.getGfwDirective() + o2.getOriData();
            return -score1.compareTo(score2);
        });
        gfwRuleEntities.clear();

        gfwRuleEntities.addAll(Arrays.asList(gfwRuleEntityArr));

        for (GFWRuleEntity gfwRuleEntity : gfwRuleEntities) {
            if (gfwRuleEntity.getGfwDirective() == GFWDirective.HOST_MATCH || gfwRuleEntity.getGfwDirective() == GFWDirective.HOST_END_WIDTH) {
                Pattern p = Pattern.compile("^[a-zA-Z0-9.\\-]+$");
                if (!p.matcher(gfwRuleEntity.getData()).find()) {
                    //log.info("{} => {}", gfwRuleEntity.getOriData(), UtilTools.objToString(gfwRuleEntity));
                    throw new RuntimeException("!p.matcher(gfwRuleEntity.getData()).find():" + gfwRuleEntity.getData());
                }
            }
            //log.info("{} => {}", gfwRuleEntity.getOriData(), UtilTools.objToString(gfwRuleEntity));
        }
    }


    public GFWRuleEntity parserGFWRuleEntity(String line) {
        GFWRuleEntity gfwRuleEntity = new GFWRuleEntity();
        gfwRuleEntity.setOriData(line);
        parserGFWRuleEntity(line, gfwRuleEntity);
        return gfwRuleEntity;
    }

    public void parserGFWRuleEntity(String line, GFWRuleEntity gfwRuleEntity) {
        if (line.startsWith("@@")) {
            line = line.substring(2);
            gfwRuleEntity.setExclude(true);
            //递归调用
            parserGFWRuleEntity(line, gfwRuleEntity);
            return;
        }


        if (line.startsWith("||")) {
            line = line.substring(2);

            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(line, true);
            gfwRuleEntity.setGfwDirective(GFWDirective.HOST_MATCH);
            gfwRuleEntity.setData(protocolInfo.getHostAddress());
            gfwRuleEntity.setProtocolInfo(protocolInfo);
            wildcardCheck(gfwRuleEntity);
            return;
        }

        if (line.startsWith("|")) {
            line = line.substring(1);
            line = URLDecoder.decode(line, StandardCharsets.UTF_8);
            gfwRuleEntity.setGfwDirective(GFWDirective.URL_MATCH);
            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(line, true);
            gfwRuleEntity.setProtocolInfo(protocolInfo);
            gfwRuleEntity.setData(protocolInfo.getHostAddress());
            wildcardCheck(gfwRuleEntity);
            return;

        }

        if (line.startsWith("/")) {
            line = line.substring(1, line.length() - 1);
            gfwRuleEntity.setGfwDirective(GFWDirective.REG_MATCH);
            gfwRuleEntity.setPattern(Pattern.compile(line));
            gfwRuleEntity.setPatternStr(line);
            return;
        }
        if (line.startsWith(".")) {
            line = URLDecoder.decode(line, StandardCharsets.UTF_8);
            gfwRuleEntity.setGfwDirective(GFWDirective.HOST_END_WIDTH);
            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(line, true);
            gfwRuleEntity.setData(protocolInfo.getHostAddress());
            gfwRuleEntity.setProtocolInfo(protocolInfo);
            wildcardCheck(gfwRuleEntity);
            return;
        }
        if (Pattern.compile("^\\d").matcher(line).find() || Pattern.compile("^\\w").matcher(line).find()) {
            line = URLDecoder.decode(line, StandardCharsets.UTF_8);
            gfwRuleEntity.setGfwDirective(GFWDirective.HOST_MATCH);
            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(line, true);
            gfwRuleEntity.setData(protocolInfo.getHostAddress());
            gfwRuleEntity.setProtocolInfo(protocolInfo);
            wildcardCheck(gfwRuleEntity);
            return;
        }
        throw new RuntimeException("Unresolved string:" + line);
    }

    public void wildcardCheck(GFWRuleEntity gfwRuleEntity) {
        if (gfwRuleEntity.getData().contains("*")) {
            String tmpStr = gfwRuleEntity.getData();
            tmpStr = tmpStr.replace(".", "\\.");
            tmpStr = tmpStr.replace("*", ".*");
            gfwRuleEntity.setPatternStr("^" + tmpStr + "$");
            gfwRuleEntity.setPattern(Pattern.compile(gfwRuleEntity.getPatternStr()));
            //更改为正则表达式校验
            gfwRuleEntity.setGfwDirective(GFWDirective.REG_MATCH);
        }
    }

    public Directive check(String address, int port) {

        InetAddressValidator ipValidator = InetAddressValidator.getInstance();

        List<String> urlLike = new ArrayList<>();

        urlLike.add(address);
        urlLike.add(String.format("%s:%d", address, port));
        if (port == 443) {
            urlLike.add(String.format("https://%s/aa/bb", address));
        } else if (port == 80) {
            urlLike.add(String.format("http://%s/aa/bb", address));
        } else {
            urlLike.add(String.format("https://%s:%d/aa/bb", address, port));
            urlLike.add(String.format("http://%s:%d/aa/bb", address, port));
        }

        log.info("address:{},port:{},urlLike:{}", address, port, UtilTools.objToString(urlLike));

        for (GFWRuleEntity gfwRuleEntity : gfwRuleEntities) {
            Directive directive = gfwRuleEntity.check(address, port, urlLike);
            if (directive != Directive.MISS) {
                log.info("check {}:{} ==>{},{}", address, port, directive, UtilTools.objToString(gfwRuleEntity));
                return directive;
            }
        }
        if (ipValidator.isValidInet6Address(address)) {
            address = UtilTools.iv6Expander(address);

            urlLike = new ArrayList<>();

            urlLike.add(address);
            urlLike.add(String.format("[%s]:%d", address, port));
            if (port == 443) {
                urlLike.add(String.format("https://[%s]/aa/bb", address));
            } else if (port == 80) {
                urlLike.add(String.format("http://[%s]/aa/bb", address));
            } else {
                urlLike.add(String.format("https://[%s]:%d/aa/bb", address, port));
                urlLike.add(String.format("http://[%s]:%d/aa/bb", address, port));
            }

            log.info("address:{},port:{},urlLike:{}", address, port, UtilTools.objToString(urlLike));

            for (GFWRuleEntity gfwRuleEntity : gfwRuleEntities) {
                Directive directive = gfwRuleEntity.check(address, port, urlLike);
                if (directive != Directive.MISS) {
                    log.info("check {}:{} ==>{},{}", address, port, directive, UtilTools.objToString(gfwRuleEntity));
                    return directive;
                }
            }
        }
        log.info("check {}:{} ==>{}", address, port, Directive.MISS);
        return super.check(address, port);

    }


    public Directive check(String address, int port, Directive defaultDirective) {
        Directive directive = check(address, port);
        if (directive == Directive.MISS) {
            return defaultDirective;
        }
        return directive;
    }

    public void download() throws InterruptedException {
        String gfwList = JproxyProperties.getProperty("local-server.gfw-list");
        download(gfwList);
    }

    public void download(String url) throws InterruptedException {
        String gfwPath = JproxyProperties.getProperty("gfw-path");

        File file = new File(gfwPath);
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("!file.delete()");
            }
        }

        Downloader downloader = new HttpDownloader();
        Promise<Channel> promise = downloader.download(url, gfwPath, true);
        promise.await().sync();
    }


    public void reload() throws IOException {
        gfwRuleEntities.clear();
        loadConfigure(true);
    }

    public String list() throws IOException {
        return UtilTools.objToString(gfwRuleEntities.reversed(), true);
    }
}
