package org.gameswap.messaging.model;

public class VerifyRequest {

    private long accountId;

    private String bggUserName;

    private String code;

    public VerifyRequest(long accountId, String bggUserName, String code) {
        this.accountId = accountId;
        this.bggUserName = bggUserName;
        this.code = code;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getBggUserName() {
        return bggUserName;
    }

    public String getCode() {
        return code;
    }
}
