package org.pierce.mybatis.entity;

public class Issuers {
    int id;
    String commonName;
    String pemCert;
    boolean isRoot;
    int parentId;
    String privateKeyRef;
    boolean active;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPrivateKeyRef() {
        return privateKeyRef;
    }

    public void setPrivateKeyRef(String privateKeyRef) {
        this.privateKeyRef = privateKeyRef;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public String getPemCert() {
        return pemCert;
    }

    public void setPemCert(String pemCert) {
        this.pemCert = pemCert;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
}
