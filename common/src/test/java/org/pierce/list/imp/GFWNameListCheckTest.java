package org.pierce.list.imp;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.Test;
import org.pierce.UtilTools;
import org.pierce.list.Directive;
import org.pierce.list.gfw.GFWDirective;
import org.pierce.list.gfw.GFWRuleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GFWNameListCheckTest {

    private static final Logger log = LoggerFactory.getLogger(GFWNameListCheckTest.class);

    @Test
    public void test001() throws IOException {
        InetAddressValidator ipValidator = InetAddressValidator.getInstance();
        if (false) {
            return;
        }
        GFWNameListCheck gfwNameListCheck = new GFWNameListCheck() {
            {
                try (InputStream is = GFWNameListCheckTest.class.getResourceAsStream("/gfw-list.txt")) {
                    if (is != null) {

                        Scanner scanner = new Scanner(is);
                        while (scanner.hasNext()) {
                            String line = scanner.nextLine();

                            System.out.println(line);

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

                                if (ipValidator.isValid(gfwRuleEntity.getData())) {
                                    continue;
                                }
                                Pattern p = Pattern.compile("^[a-zA-Z0-9.\\-]+$");

                                if (!p.matcher(gfwRuleEntity.getData()).find()) {
                                    log.info("{} => {}", gfwRuleEntity.getOriData(), UtilTools.objToString(gfwRuleEntity));
                                    throw new RuntimeException("!p.matcher(gfwRuleEntity.getData()).find():" + gfwRuleEntity.getData());
                                }
                            }
                            //log.info("{} => {}", gfwRuleEntity.getOriData(), UtilTools.objToString(gfwRuleEntity));
                        }
                    }
                }


            }
        };
        //gfwNameListCheck.check("vllcs.org",80);
        //gfwNameListCheck.check("vllcs.org",80);
		//flow is base64
//ICAgICAgICBjaGVjayhnZndOYW1lTGlzdENoZWNrLCAiaHVvYmkuY29tIiwgNDQzKTsvLw0KICAgICAgICBjaGVjayhnZndOYW1lTGlzdENoZWNrLCAiMjYwNzpmODEwOjQwMDc6ODBmOjoyMDAzIiwgODApOw0KICAgICAgICBjaGVjayhnZndOYW1lTGlzdENoZWNrLCAiMjYwNzpmODEwOjQwMDc6MDgwZjowMDAwOjAwMDA6MDAwMDoyMDAzIiwgODApOw0KICAgICAgICBjaGVjayhnZndOYW1lTGlzdENoZWNrLCAiZ29vZ2xlLmNvbSIsIDQ0Myk7DQogICAgICAgIGNoZWNrKGdmd05hbWVMaXN0Q2hlY2ssICJzc2wuZ3N0YXRpYy5jb20iLCA0NDMpOy8vY2hlY2soZ2Z3TmFtZUxpc3RDaGVjaywgInguYmxvZ3Nwb3QuY29tIik7Ly8
    }

  /*  public void check(GFWNameListCheck gfwNameListCheck, String address, int port) {
        System.out.printf("%s:%s", address, gfwNameListCheck.check(address, 443));

    }*/

    public void check(GFWNameListCheck gfwNameListCheck, String address, int port) {
        System.out.printf("%s:%d ==> %s\n", address, port, gfwNameListCheck.check(address, port));
    }

    @Test
    public void test002() {
        String gfwStr = "||addons.mozilla.org/*-*/firefox/addon/ublock-origin/*";
    }

    @Test
    public void test003() {
        ///^https?:\/\/(?=.*?(2x3|ni5|j5o))[a-z0-9.-]+\.xn--ngstr-lra8j\.com$ => {"exclude":true,"gfwDirective":"REG_MATCH","oriData":"@@/^https?:\\/\\/(?\u003d.*?(2x3|ni5|j5o))[a-z0-9.-]+\\.xn--ngstr-lra8j\\.com$","patternStr":"^https?:\\/\\/(?\u003d.*?(2x3|ni5|j5o))[a-z0-9.-]+\\.xn--ngstr-lra8j\\.com","data":null,"protocolInfo":null}
        String str = "^https?://(?=.*?(2x3|ni5|j5o))[a-z0-9.-]+\\.xn--ngstr-lra8j\\.com$";
        System.out.printf("str:%s\n", str);
        Pattern p = Pattern.compile(str);
    }

    @Test
    public void test004() {
        String str = "|http://*2.bahamut.com.tw";
        GFWNameListCheck gfwNameListCheck = new GFWNameListCheck();
        GFWRuleEntity gfwRuleEntity = gfwNameListCheck.parserGFWRuleEntity(str);
        System.out.printf("gfwRuleEntity:%s\n", UtilTools.objToString(gfwRuleEntity));
        if (gfwRuleEntity.getPattern().matcher("a2.bahamut.com.tw").find()) {
            System.out.println("matched``");
        }
    }

    @Test
    public void test005() {
        String str = "2607:f8b0:4007:80f::2003";
        GFWNameListCheck gfwNameListCheck = new GFWNameListCheck();
        GFWRuleEntity gfwRuleEntity = gfwNameListCheck.parserGFWRuleEntity(str);
        System.out.printf("gfwRuleEntity:%s\n", UtilTools.objToString(gfwRuleEntity));
        Directive directive = gfwRuleEntity.check("2607:f8b0:4007:080f:0000:0000:0000:2003", 443, new LinkedList<>());
        System.out.println(directive);
        directive = gfwRuleEntity.check("2607:f8b0:4007:80f::2003", 443, new LinkedList<>());
        System.out.println(directive);
    }

    @Test
    public void test006() throws IOException {

        GFWNameListCheck gfwNameListCheck = GFWNameListCheck.getInstance();
		//flow is base64
//ICAgICAgICBhc3NlcnQgRGlyZWN0aXZlLkZVTExfQ09OTkVDVC5lcXVhbHMoZ2Z3TmFtZUxpc3RDaGVjay5jaGVjaygiLjE3N3BpYy5pbmZvIiwgNDQzKSk7DQogICAgICAgIGFzc2VydCBEaXJlY3RpdmUuRlVMTF9DT05ORUNULmVxdWFscyhnZndOYW1lTGlzdENoZWNrLmNoZWNrKCJ4eHl5LjE3N3BpYy5pbmZvIiwgNDQzKSk7DQogICAgICAgIGFzc2VydCBEaXJlY3RpdmUuRlVMTF9DT05ORUNULmVxdWFscyhnZndOYW1lTGlzdENoZWNrLmNoZWNrKCIuYnVzaW5lc3NpbnNpZGVyLmNvbSIsIDQ0MykpOw0KICAgICAgICBhc3NlcnQgRGlyZWN0aXZlLkZVTExfQ09OTkVDVC5lcXVhbHMoZ2Z3TmFtZUxpc3RDaGVjay5jaGVjaygieHh5eS5idXNpbmVzc2luc2lkZXIuY29tIiwgNDQzKSk7DQogICAgICAgIGFzc2VydCBEZWZhdWx0TmFtZUxpc3RDaGVjay5ob3N0TmFtZURlZmF1bHQuZXF1YWxzKGdmd05hbWVMaXN0Q2hlY2suY2hlY2soImFhYmJjY2RkLmNvbSIsIDQ0MykpOw0KICAgICAgICBhc3NlcnQgRGlyZWN0aXZlLkZVTExfQ09OTkVDVC5lcXVhbHMoZ2Z3TmFtZUxpc3RDaGVjay5jaGVjaygiYWJjLmdvb2dsZS5jb20iLCA4MCkpOw0KICAgICAgICAvL2Fzc2VydCBEaXJlY3RpdmUuRlVMTF9DT05ORUNULmVxdWFscyhnZndOYW1lTGlzdENoZWNrLmNoZWNrKCJ0d2ltZy5lZGdlc3VpdGUubmV0IiwgODApKTsvL2ZhaWwNCiAgICAgICAgYXNzZXJ0IERpcmVjdGl2ZS5ESVJFQ1RfQ09OTkVDVC5lcXVhbHMoZ2Z3TmFtZUxpc3RDaGVjay5jaGVjaygieW91ZGFvLmNvbSIsIDQ0MykpOw0KICAgICAgICBhc3NlcnQgRGlyZWN0aXZlLkZVTExfQ09OTkVDVC5lcXVhbHMoZ2Z3TmFtZUxpc3RDaGVjay5jaGVjaygiYXNkZmcuanAiLCAyMikpOw0KICAgICAgICBhc3NlcnQgRGlyZWN0aXZlLkZVTExfQ09OTkVDVC5lcXVhbHMoZ2Z3TmFtZUxpc3RDaGVjay5jaGVjaygiaGlkZWNsb3VkLmNvbSIsIDIyKSk7DQogICAgICAgIGFzc2VydCBEaXJlY3RpdmUuRlVMTF9DT05ORUNULmVxdWFscyhnZndOYW1lTGlzdENoZWNrLmNoZWNrKCJ4eHl5Lm0tdGVhbS5jYyIsIDIyKSk7DQogICAgICAgIGFzc2VydCBEaXJlY3RpdmUuRlVMTF9DT05ORUNULmVxdWFscyhnZndOYW1lTGlzdENoZWNrLmNoZWNrKCJ4Mi5iYWhhbXV0LmNvbS50dyIsIDIyKSk7DQogICAgICAgIGFzc2VydCBEaXJlY3RpdmUuRlVMTF9DT05ORUNULmVxdWFscyhnZndOYW1lTGlzdENoZWNrLmNoZWNrKCJjZG5hYWYuc2VhcmNoLnh4eCIsIDIyKSk7DQogICAgICAgIGFzc2VydCBEaXJlY3RpdmUuRlVMTF9DT05ORUNULmVxdWFscyhnZndOYW1lTGlzdENoZWNrLmNoZWNrKCJhZGRvbnMubW96aWxsYS5vcmciLCAyMikpOw0KICAgICAgICBhc3NlcnQgRGlyZWN0aXZlLkZVTExfQ09OTkVDVC5lcXVhbHMoZ2Z3TmFtZUxpc3RDaGVjay5jaGVjaygiZ2V0dHlpbWFnZXMueHl6IiwgMjIpKTs
    }

}
