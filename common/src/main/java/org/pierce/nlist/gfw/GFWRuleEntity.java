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

    public Directive check(String address, int port) {

        List<String> stringList = new ArrayList<>();
        stringList.add(address);
        stringList.add(String.format("%s:%d", address, port));
        if (port == 443) {
            stringList.add(String.format("https://%s/aa/bb", address));
        } else if (port == 80) {
            stringList.add(String.format("http://%s/aa/bb", address));
        } else {
            stringList.add(String.format("https://%s:%d/aa/bb", address, port));
            stringList.add(String.format("http://%s:%d/aa/bb", address, port));
        }
        boolean patterTest = false;
        if (pattern != null) {
            for (String str : stringList) {
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

    public Directive check(String address) {
        return check(address, -1);

    }
}
