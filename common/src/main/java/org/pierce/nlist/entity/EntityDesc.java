package org.pierce.nlist.entity;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.pierce.nlist.Directive;
import org.pierce.nlist.MatchType;
import org.pierce.nlist.util.NetTools;

import java.util.regex.Pattern;

public class EntityDesc {

    Directive directive;

    MatchType matchType;

    String data;

    Pattern pattern;


    String address;

    int cidrLen;

    public Directive getDirective() {
        return directive;
    }

    public void setDirective(Directive directive) {
        this.directive = directive;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCidrLen() {
        return cidrLen;
    }

    public void setCidrLen(int cidrLen) {
        this.cidrLen = cidrLen;
    }

    public Directive test(String input) {
        if (matchType == MatchType.EQUAL) {
            if (data.equals(input)) {
                return directive;
            }
        } else if (matchType == MatchType.REGULAR_MATCHING) {
            if (pattern.matcher(input).find()) {
                return directive;
            }
        } else if (matchType == MatchType.SUBNET) {
            InetAddressValidator ipValidator = InetAddressValidator.getInstance();
            if (ipValidator.isValidInet4Address(input)) {
                if (NetTools.sampleNet(address, input, cidrLen)) {
                    return directive;
                }
            }

        }
        return Directive.MISS;
    }
}
