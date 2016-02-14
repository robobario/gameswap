package org.gameswap.web.authentication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.gameswap.web.model.Token;
import org.joda.time.DateTime;

import java.text.ParseException;

public class JwtTokenCoder {

    public static final String AUTH_HEADER_KEY = "Authorization";
    private static final JWSHeader JWT_HEADER = new JWSHeader(JWSAlgorithm.HS256);
    private final String tokenSecret;

    public JwtTokenCoder(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    private static String getSerializedToken(String authHeader) {
        return authHeader.split(" ")[1];
    }

    public String getSubject(String authHeader) throws ParseException, JOSEException {
        return decodeToken(authHeader).getSubject();
    }

    public JWTClaimsSet decodeToken(String authHeader) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(getSerializedToken(authHeader));
        if (signedJWT.verify(new MACVerifier(tokenSecret))) {
            return signedJWT.getJWTClaimsSet();
        } else {
            throw new JOSEException("Signature verification failed");
        }
    }

    public Token createToken(String host, long userId, String displayName, String role) throws JOSEException {
        JWTClaimsSet.Builder claim = new JWTClaimsSet.Builder();
        claim.subject(Long.toString(userId));
        claim.issuer(host);
        DateTime now = getNow();
        claim.issueTime(now.toDate());
        claim.expirationTime(now.plusDays(14).toDate());
        claim.claim("name", displayName);
        claim.claim("role", role);
        JWSSigner signer = new MACSigner(tokenSecret);
        SignedJWT jwt = new SignedJWT(JWT_HEADER, claim.build());
        jwt.sign(signer);
        return new Token(jwt.serialize());
    }

    protected DateTime getNow() {
        return DateTime.now();
    }
}