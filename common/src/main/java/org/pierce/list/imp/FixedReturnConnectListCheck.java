package org.pierce.list.imp;

import org.pierce.list.Directive;
import org.pierce.list.NameListCheck;

public class FixedReturnConnectListCheck extends DefaultNameListCheck implements NameListCheck {

    Directive directive = Directive.FULL_CONNECT;

    public FixedReturnConnectListCheck() {
    }

    public FixedReturnConnectListCheck(Directive directive) {
        this.directive = directive;
    }

    @Override
    public Directive check(String name, int port) {
        return directive;
    }

/*    @Override
    public Directive check(String name, int port, Directive defaultDirective) {
        return directive;
    }*/
}
