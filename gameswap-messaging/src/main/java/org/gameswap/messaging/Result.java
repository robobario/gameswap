package org.gameswap.messaging;

import com.google.common.base.MoreObjects;

public class Result {
    private final boolean success;

    private final String errorMessage;

    public Result(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("success", success)
                .add("errorMessage", errorMessage)
                .toString();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
