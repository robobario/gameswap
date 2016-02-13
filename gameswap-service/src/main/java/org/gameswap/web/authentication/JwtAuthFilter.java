package org.gameswap.web.authentication;

import com.google.common.base.Optional;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.lang3.StringUtils;
import org.gameswap.model.User;
import org.gameswap.model.UserPrincipal;
import org.gameswap.persistance.UserDAO;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;
import org.joda.time.DateTime;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;

@Priority(1000)
public class JwtAuthFilter implements ContainerRequestFilter {
    private UserDAO dao;
    private SessionFactory sessionFactory;
    private JwtTokenCoder jwtTokenCoder;

    public JwtAuthFilter(UserDAO dao, SessionFactory sessionFactory, JwtTokenCoder jwtTokenCoder) {
        this.dao = dao;
        this.sessionFactory = sessionFactory;
        this.jwtTokenCoder = jwtTokenCoder;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString(JwtTokenCoder.AUTH_HEADER_KEY);
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.setDefaultReadOnly(true);
            session.setCacheMode(CacheMode.NORMAL);
            session.setFlushMode(FlushMode.MANUAL);
            ManagedSessionContext.bind(session);
            doWork(requestContext, authHeader);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void doWork(final ContainerRequestContext requestContext, String authHeader) {
        if (!StringUtils.isBlank(authHeader) && authHeader.split(" ").length == 2) {
            JWTClaimsSet claimSet;
            try {
                claimSet = jwtTokenCoder.decodeToken(authHeader);
            } catch (ParseException | JOSEException e) {
                throw new NotAuthorizedException(e);
            }

            // ensure that the token is not expired
            if (!new DateTime(claimSet.getExpirationTime()).isBefore(DateTime.now())) {
                Optional<User> userOptional = dao.find(Long.parseLong(claimSet.getSubject()));
                Optional<UserPrincipal> transform = userOptional.transform(user -> {
                    UserPrincipal principal = new UserPrincipal(user.getUsername());
                    principal.addRole(user.getRole());
                    return principal;
                });
                if (transform.isPresent()) {
                    UserPrincipal userPrincipal = transform.get();
                    requestContext.setSecurityContext(new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() {
                            return userPrincipal;
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            return userPrincipal.isUserInRole(role);
                        }

                        @Override
                        public boolean isSecure() {
                            return requestContext.getSecurityContext().isSecure();
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return "JWT";
                        }
                    });
                }

            }
        }
    }
}
