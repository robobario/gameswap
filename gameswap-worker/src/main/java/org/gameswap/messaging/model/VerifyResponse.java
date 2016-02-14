package org.gameswap.messaging.model;

public class VerifyResponse {

    private long accountId;

    private String bggUserName;

    private boolean success;

    public VerifyResponse(long accountId, String bggUserName, boolean success) {
        this.accountId = accountId;
        this.bggUserName = bggUserName;
        this.success = success;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getBggUserName() {
        return bggUserName;
    }

    public boolean isSuccess() {
        return success;
    }
}
