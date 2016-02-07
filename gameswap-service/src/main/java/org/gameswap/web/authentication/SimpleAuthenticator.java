package org.gameswap.web.authentication;

import com.google.common.base.Optional;

import org.gameswap.model.User;
import org.gameswap.model.UserPrincipal;
import org.gameswap.persistance.UserDAO;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

public class SimpleAuthenticator implements Authenticator<BasicCredentials, UserPrincipal> {

    private UserDAO userDAO;

    public SimpleAuthenticator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public Optional<UserPrincipal> authenticate(BasicCredentials credentials) throws AuthenticationException {
        Optional<User> userOptional = userDAO.findByName(credentials.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(credentials.getPassword())) {
                return Optional.of(new UserPrincipal(credentials.getUsername()));
            }
        }
        return Optional.absent();
    }
}