package org.pierce.nlist;

public interface NameListCheck {
    Directive check(String name);
    Directive check(String name, Directive defaultDirective);
}
