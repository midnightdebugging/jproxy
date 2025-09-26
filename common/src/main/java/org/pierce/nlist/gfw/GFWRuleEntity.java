package org.pierce.nlist.gfw;

import org.pierce.entity.ProtocolInfo;
import org.pierce.nlist.Directive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GFWRuleEntity {

    private static final Logger log = LoggerFactory.getLogger(GFWRuleEntity.class);

    boolean exclude;
    GFWDirective gfwDirective;
    String oriData;
    transient Pattern pattern;
    String patternStr;
    String data;
    ProtocolInfo protocolInfo;

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public GFWDirective getGfwDirective() {
        return gfwDirective;
    }

    public void setGfwDirective(GFWDirective gfwDirective) {
        this.gfwDirective = gfwDirective;
    }

    public String getOriData() {
        return oriData;
    }

    public void setOriData(String oriData) {
        this.oriData = oriData;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ProtocolInfo getProtocolInfo() {
        return protocolInfo;
    }

    public void setProtocolInfo(ProtocolInfo protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    public String getPatternStr() {
        return patternStr;
    }

    public void setPatternStr(String patternStr) {
        this.patternStr = patternStr;
    }

    public Directive check(String address, int port, List<String> urlLike) {

        boolean patterTest = false;
        if (pattern != null) {
            for (String str : urlLike) {
                if (pattern.matcher(str).find()) {
                    patterTest = true;
                }
            }
        }

        switch (gfwDirective) {
            // like //
            case REG_MATCH:
                if (patterTest) {
                    return exclude ? Directive.DIRECT_CONNECT : Directive.FULL_CONNECT;
                }
                break;
            // like |
            case URL_MATCH:

/*                if (protocolInfo.getHostAddress().contains("vllcs.org")) {
                    String tmp = protocolInfo.getHostAddress();
                    System.out.println(protocolInfo.getHostAddress());
                    System.out.println(address);
                    System.out.println(protocolInfo.getHostAddress().equals(address));
                }*/
                if (protocolInfo.getHostAddress().equals(address) || patterTest) {
                    if (protocolInfo.getPort() == port) {
                        return exclude ? Directive.DIRECT_CONNECT : Directive.FULL_CONNECT;
                    }
                    if (!exclude) {
                        return Directive.FULL_CONNECT;
                    }
                }
                break;
            // like |
            case HOST_MATCH:
                if (address.equals(data) || patterTest) {
                    return exclude ? Directive.DIRECT_CONNECT : Directive.FULL_CONNECT;
                }
                break;
            // is start width
            case HOST_END_WIDTH:
                if (address.endsWith(data) || patterTest) {
                    return exclude ? Directive.DIRECT_CONNECT : Directive.FULL_CONNECT;
                }
                break;
        }
        return Directive.MISS;

    }
}
