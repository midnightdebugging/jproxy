package org.pierce.entity;

import org.pierce.nlist.Directive;

public class ConnectType {
    Directive directive;
    String address;

    public ConnectType(Directive directive, String address) {
        this.directive = directive;
        this.address = address;
    }

    public Directive getDirective() {
        return directive;
    }

    public void setDirective(Directive directive) {
        this.directive = directive;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
