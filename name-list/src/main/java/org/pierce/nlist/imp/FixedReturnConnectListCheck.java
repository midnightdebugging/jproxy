package org.pierce.nlist.imp;

import org.pierce.nlist.Directive;
import org.pierce.nlist.NameListCheck;

public class FixedReturnConnectListCheck implements NameListCheck {

    @Override
    public Directive check(String name) {
        return Directive.FULL_CONNECT;
    }

    @Override
    public Directive check(String name, Directive defaultDirective) {
        return Directive.FULL_CONNECT;
    }
}
