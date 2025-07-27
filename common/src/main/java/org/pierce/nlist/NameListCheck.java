package org.pierce.nlist;

public interface NameListCheck {

    Directive check(String address, int port);

    //Directive check(String address, int port, Directive defaultDirective);
}
