package org.pierce.handler;

import io.netty.channel.Channel;

public class LinkOutStatusEvent {

    LinkOutStep linkOutStep;

    boolean success;


    transient Throwable cause;

    public LinkOutStatusEvent() {
    }

    public LinkOutStatusEvent(LinkOutStep linkOutStep, boolean success) {
        this.linkOutStep = linkOutStep;
        this.success = success;
    }

    public LinkOutStatusEvent(LinkOutStep linkOutStep, boolean success, Throwable cause) {
        this.linkOutStep = linkOutStep;
        this.success = success;
        this.cause = cause;
    }

    public LinkOutStep getLinkOutStep() {
        return linkOutStep;
    }

    public void setLinkOutStep(LinkOutStep linkOutStep) {
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
}
