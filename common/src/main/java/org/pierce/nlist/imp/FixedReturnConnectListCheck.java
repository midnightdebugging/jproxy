package org.pierce.nlist.imp;

import org.pierce.nlist.Directive;
import org.pierce.nlist.NameListCheck;

public class FixedReturnConnectListCheck extends DefaultNameListCheck  implements NameListCheck {

    final Directive directive = Directive.FULL_CONNECT;

    @Override
    public Directive check(String name, int port) {
        return directive;
    }

/*    @Override
    public Directive check(String name, int port, Directive defaultDirective) {
        return directive;
    }*/
}
