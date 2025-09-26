package org.pierce;

import org.junit.Test;
import org.pierce.nlist.Directive;
import org.pierce.nlist.gfw.GFWRuleEntity;
import org.pierce.nlist.imp.GFWNameListCheck;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class GFWNameListCheckTest {
    @Test
    public void test001() throws IOException {
        GFWNameListCheck gfwNameListCheck = new GFWNameListCheck() {
            {
                loadConfigure();
            }
        };
        //gfwNameListCheck.check("vllcs.org",80);
        //gfwNameListCheck.check("vllcs.org",80);
        check(gfwNameListCheck, "x.blogspot.com", 443);//
        check(gfwNameListCheck, "www.google.cn", 80);
        check(gfwNameListCheck, "google.com", 443);
        check(gfwNameListCheck, "ssl.gstatic.com", 443);//check(gfwNameListCheck, "x.blogspot.com");//
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
        String str="|http://*2.bahamut.com.tw";
        GFWNameListCheck gfwNameListCheck=new GFWNameListCheck();
        GFWRuleEntity gfwRuleEntity = gfwNameListCheck.parserGFWRuleEntity(str);
        System.out.printf("gfwRuleEntity:%s\n",UtilTools.objToString(gfwRuleEntity));
        if(gfwRuleEntity.getPattern().matcher("a2.bahamut.com.tw").find()){
            System.out.println("matched``");
        }
    }

}
