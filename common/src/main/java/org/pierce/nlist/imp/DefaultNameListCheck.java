package org.pierce.nlist.imp;

import org.apache.commons.validator.routines.InetAddressValidator;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

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
