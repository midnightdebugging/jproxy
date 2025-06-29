package org.pierce.entity;

public class LocalLinkStatusEvent {

    LocalLinkStep linkOutStep;

    boolean success;

    String address;

    int port;

    transient Throwable cause;

    public LocalLinkStatusEvent() {
    }

    public LocalLinkStatusEvent(LocalLinkStep linkOutStep, boolean success) {
        this.linkOutStep = linkOutStep;
        this.success = success;
    }


    public LocalLinkStatusEvent(LocalLinkStep linkOutStep, boolean success, Throwable cause) {
        this.linkOutStep = linkOutStep;
        this.success = success;
        this.cause = cause;
    }

    public LocalLinkStatusEvent(LocalLinkStep linkOutStep, boolean success, String address, int port) {
        this.linkOutStep = linkOutStep;
        this.success = success;
        this.address = address;
        this.port = port;
    }

    public LocalLinkStep getLinkOutStep() {
        return linkOutStep;
    }

    public void setLinkOutStep(LocalLinkStep linkOutStep) {
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
}
