package org.pierce.list.imp;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.pierce.list.Directive;
import org.pierce.list.NameListCheck;

public class DefaultNameListCheck implements NameListCheck {

    @Override
    public Directive check(String address, int port) {
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        if (inetAddressValidator.isValidInet4Address(address) || inetAddressValidator.isValidInet6Address(address)) {
            return Directive.DIRECT_CONNECT;
        }
        return Directive.DOMAIN_NAME_QUERY_FIRST;
    }
}
