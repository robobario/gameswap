package org.gameswap.web.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RequestVerification {

    @NotNull
    @Valid
    @Size(min = 0, max = 255)
    private String bggUserName;

    public RequestVerification() {
    }

    public String getBggUserName() {
        return bggUserName;
    }

    public void setBggUserName(String bggUserName) {
        this.bggUserName = bggUserName;
    }
}
