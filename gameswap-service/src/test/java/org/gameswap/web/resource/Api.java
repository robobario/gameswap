package org.gameswap.web.resource;

import org.gameswap.web.model.Role;
import org.gameswap.web.model.User;

public class Api {

    public static User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setDisplayName(username);
        user.setPassword(password);
        user.setRole(Role.USER);
        return user;
    }


    public static ResponseHelpers helpers() {
        return new ResponseHelpers();
    }
}
