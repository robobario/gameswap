package org.gameswap.model;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class UserPrincipal implements Principal {

    private String username;

    private List<String> roles = new ArrayList<>();


    public UserPrincipal(String username) {
        this.username = username;
    }


    public List<String> getRoles() {
        return roles;
    }


    public boolean isUserInRole(String roleToCheck) {
        return roles.contains(roleToCheck);
    }


    public String getUsername() {
        return username;
    }


    @Override
    public String getName() {
        return username;
    }


    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
