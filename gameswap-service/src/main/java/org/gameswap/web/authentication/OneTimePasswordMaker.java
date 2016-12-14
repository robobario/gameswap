package org.gameswap.web.authentication;

import org.apache.commons.lang3.RandomStringUtils;

public class OneTimePasswordMaker {
    public String getOneTimePassword(){
        return RandomStringUtils.randomAlphanumeric(6);
    }
}
