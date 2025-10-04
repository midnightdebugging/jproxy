package org.pierce.list.imp;

import org.pierce.list.Directive;
import org.pierce.list.NameListCheck;

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
