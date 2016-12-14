package org.gameswap.messaging.model;

public class VerifyRequest {

    private long accountId;

    private String bggUserName;

    private String code;

    public VerifyRequest(){}

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
