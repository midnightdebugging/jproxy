package org.pierce.rsocks.entity;


import org.pierce.codec.SocksCommand;

public class RemoteLinkStatusEvent {

    RemoteLinkStep linkOutStep;

    boolean success;

    String address;

    int port;

    transient Throwable cause;

    SocksCommand msg;

    public RemoteLinkStatusEvent() {
    }

    public RemoteLinkStatusEvent(RemoteLinkStep linkOutStep, boolean success) {
        this.linkOutStep = linkOutStep;
        this.success = success;
    }


    public RemoteLinkStatusEvent(RemoteLinkStep linkOutStep, boolean success, Throwable cause) {
        this.linkOutStep = linkOutStep;
        this.success = success;
        this.cause = cause;
    }

    public RemoteLinkStatusEvent(RemoteLinkStep linkOutStep, boolean success, String address, int port) {
        this.linkOutStep = linkOutStep;
        this.success = success;
        this.address = address;
        this.port = port;
    }

    public RemoteLinkStep getLinkOutStep() {
        return linkOutStep;
    }

    public void setLinkOutStep(RemoteLinkStep linkOutStep) {
        this.linkOutStep = linkOutStep;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public SocksCommand getMsg() {
        return msg;
    }

    public void setMsg(SocksCommand msg) {
        this.msg = msg;
    }
}
