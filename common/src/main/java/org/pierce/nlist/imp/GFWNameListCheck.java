package org.pierce.nlist.imp;

import org.pierce.JproxyProperties;
import org.pierce.UtilTools;
import org.pierce.entity.ProtocolInfo;
import org.pierce.nlist.Directive;
import org.pierce.nlist.NameListCheck;
import org.pierce.nlist.gfw.Base64InputStream;
import org.pierce.nlist.gfw.GFWDirective;
import org.pierce.nlist.gfw.GFWRuleEntity;
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

    List<GFWRuleEntity> gfwRuleEntities = new ArrayList<>();


    public GFWNameListCheck() {
    }

    public GFWNameListCheck(InputStream is) throws IOException {
        parser(is);
    }

    public void loadConfigure() throws IOException {
        String listPath = JproxyProperties.getProperty("gfw-path");
        File file = new File(listPath);
        if (!file.isFile()) {
            //throw new RuntimeException();
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

        gfwRuleEntities = Arrays.asList(gfwRuleEntityArr);

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

            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(line);
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
            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(line);
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
            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(line);
            gfwRuleEntity.setData(protocolInfo.getHostAddress());
            gfwRuleEntity.setProtocolInfo(protocolInfo);
            wildcardCheck(gfwRuleEntity);
            return;
        }
        if (Pattern.compile("^\\d").matcher(line).find() || Pattern.compile("^\\w").matcher(line).find()) {
            line = URLDecoder.decode(line, StandardCharsets.UTF_8);
            gfwRuleEntity.setGfwDirective(GFWDirective.HOST_MATCH);
            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(line);
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
            //log.info("check {}:{} [{}] {}", address, String.valueOf(port), i, gfwRuleEntity.getOriData());
            /*if(i==499){
                System.out.println(i);
            }*/
            Directive directive = gfwRuleEntity.check(address, port, urlLike);
            if (directive != Directive.MISS) {
                log.info("check {}:{} ==>{},{}", address, port, directive, UtilTools.objToString(gfwRuleEntity));
                return directive;
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
}
