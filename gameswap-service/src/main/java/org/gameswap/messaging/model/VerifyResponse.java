package org.gameswap.messaging.model;

public class VerifyResponse {

    private long accountId;

    private String bggUserName;

    private boolean success;

    public VerifyResponse(){}

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getBggUserName() {
        return bggUserName;
    }

    public void setBggUserName(String bggUserName) {
        this.bggUserName = bggUserName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
