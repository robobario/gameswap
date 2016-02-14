package org.gameswap.web.authentication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import org.assertj.core.api.MapAssert;
import org.gameswap.web.model.Token;
import org.joda.time.DateTime;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.Assert.assertEquals;

public class JwtTokenCoderTest {

    public static final String SECRET = "big old secret token of at least 32 bytes";
    public static final long USER_ID = 5L;
    public static final String DISPLAY_NAME = "name";
    public static final String ROLE = "user";
    public static final String HOST = "host";
    public static final DateTime TIME = DateTime.now();
    public static final String EXISTING_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1Iiwicm9sZSI6InVzZXIiLCJpc3MiOiJob3N0IiwibmFtZSI6Im5hbWUiLCJleHAiOjE0NTY1MzUzODksImlhdCI6MTQ1NTMyNTc4OX0.8l-SE3VLTumI8TOoEKGMO7wiCXXeWkWxAk-rot6CUdo";
    public static final String ANOTHER_SECRET = "wrong secret bad secret wrong secret another secret";

    @Test
    public void encodedClaimsCanBeDecoded() throws JOSEException, ParseException {
        JwtTokenCoder tokenCoder = getJwtTokenCoder(SECRET, TIME);
        Token token = create(tokenCoder, HOST, USER_ID, DISPLAY_NAME, ROLE);
        JWTClaimsSet claimsSet = decode(tokenCoder, token.getToken());
        assertEquals(Long.toString(USER_ID), claimsSet.getSubject());
        MapAssert<String, Object> claims = assertThat(claimsSet.getClaims());
        claims.contains(entry("sub", Long.toString(USER_ID)));
        claims.contains(entry("role", ROLE));
        claims.contains(entry("name", DISPLAY_NAME));
        claims.contains(entry("iss", HOST));
        claims.contains(entry("iat", TIME.withMillisOfSecond(0).toDate()));
        claims.contains(entry("exp", TIME.plusDays(14).withMillisOfSecond(0).toDate()));
    }


    @Test
    public void existingClaimsCanBeDecoded() throws JOSEException, ParseException {
        JwtTokenCoder tokenCoder = getJwtTokenCoder(SECRET, TIME);
        JWTClaimsSet claimsSet = decode(tokenCoder, EXISTING_TOKEN);
        MapAssert<String, Object> claims = assertThat(claimsSet.getClaims());
        claims.contains(entry("sub", Long.toString(USER_ID)));
        claims.contains(entry("role", ROLE));
        claims.contains(entry("name", DISPLAY_NAME));
        claims.contains(entry("iss", HOST));
        claims.contains(entry("iat", new Date(1455325789000L)));
        claims.contains(entry("exp", new Date(1456535389000L)));
    }

    @Test(expected = JOSEException.class)
    public void claimsCantBeDecodedByAnotherSecret() throws JOSEException, ParseException {
        JwtTokenCoder tokenCoder = getJwtTokenCoder(SECRET, TIME);
        JwtTokenCoder tokenCoder2 = getJwtTokenCoder(ANOTHER_SECRET, TIME);

        Token token = create(tokenCoder, HOST, USER_ID, DISPLAY_NAME, ROLE);
        decode(tokenCoder2, token.getToken());
    }

    @Test
    public void expiryAndIssueTimeSet() throws JOSEException, ParseException {
        JwtTokenCoder tokenCoder = getJwtTokenCoder(SECRET, TIME);
        Token token = create(tokenCoder, HOST, USER_ID, DISPLAY_NAME, ROLE);
        JWTClaimsSet claimsSet = decode(tokenCoder, token.getToken());
        MapAssert<String, Object> claims = assertThat(claimsSet.getClaims());
        claims.contains(entry("iat", TIME.withMillisOfSecond(0).toDate()));
        claims.contains(entry("exp", TIME.plusDays(14).withMillisOfSecond(0).toDate()));
    }

    @Test
    public void getSubject() throws JOSEException, ParseException {
        JwtTokenCoder tokenCoder = getJwtTokenCoder(SECRET, TIME);
        Token token = create(tokenCoder, HOST, USER_ID, DISPLAY_NAME, ROLE);
        String subject = tokenCoder.getSubject("Bearer " + token.getToken());
        assertEquals(Long.toString(USER_ID), subject);
    }

    private Token create(JwtTokenCoder tokenCoder, String host, long userId, String displayName, String role) throws JOSEException {
        return tokenCoder.createToken(host, userId, displayName, role);
    }

    private JWTClaimsSet decode(JwtTokenCoder tokenCoder, String token) throws ParseException, JOSEException {
        return tokenCoder.decodeToken("Bearer " + token);
    }

    private JwtTokenCoder getJwtTokenCoder(final String token, final DateTime time) {
        return new JwtTokenCoder(token) {
            @Override
            protected DateTime getNow() {
                return time;
            }
        };
    }

}