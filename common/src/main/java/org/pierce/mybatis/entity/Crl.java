package org.pierce.mybatis.entity;

public class Crl {
    int id;
    int issuerId;
    String thisUpdate;
    String nextUpdate;
    int crlNumber;
    String crlData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(int issuerId) {
        this.issuerId = issuerId;
    }

    public String getThisUpdate() {
        return thisUpdate;
    }

    public void setThisUpdate(String thisUpdate) {
        this.thisUpdate = thisUpdate;
    }

    public String getNextUpdate() {
        return nextUpdate;
    }

    public void setNextUpdate(String nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    public int getCrlNumber() {
        return crlNumber;
    }

    public void setCrlNumber(int crlNumber) {
        this.crlNumber = crlNumber;
    }

    public String getCrlData() {
        return crlData;
    }

    public void setCrlData(String crlData) {
        this.crlData = crlData;
    }
}
