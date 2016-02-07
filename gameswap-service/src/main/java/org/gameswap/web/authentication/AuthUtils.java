package org.gameswap.web.authentication;

import java.text.ParseException;

import com.nimbusds.jose.crypto.MACVerifier;
import org.gameswap.model.Token;
import org.joda.time.DateTime;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public final class AuthUtils {
	
	private static final JWSHeader JWT_HEADER = new JWSHeader(JWSAlgorithm.HS256);
	private static final String TOKEN_SECRET = "aliceinwonderlandhajiddiwhatnowzaheyheyhey";
	public static final String AUTH_HEADER_KEY = "Authorization";
	
	public static String getSubject(String authHeader) throws ParseException, JOSEException {
		return decodeToken(authHeader).getSubject();
	}
	
	public static JWTClaimsSet decodeToken(String authHeader) throws ParseException, JOSEException {
		SignedJWT signedJWT = SignedJWT.parse(getSerializedToken(authHeader));
		if (signedJWT.verify(new MACVerifier(TOKEN_SECRET))) {
			return signedJWT.getJWTClaimsSet();
		} else {
			throw new JOSEException("Signature verification failed");
		}
	}
	
	public static Token createToken(String host, long sub) throws JOSEException {
		JWTClaimsSet.Builder claim = new JWTClaimsSet.Builder();
		claim.subject(Long.toString(sub));
		claim.issuer(host);
		claim.issueTime(DateTime.now().toDate());
		claim.expirationTime(DateTime.now().plusDays(14).toDate());
		JWSSigner signer = new MACSigner(TOKEN_SECRET);
		SignedJWT jwt = new SignedJWT(JWT_HEADER, claim.build());
		jwt.sign(signer);
		
		return new Token(jwt.serialize());
	}
	
	public static String getSerializedToken(String authHeader) {
		return authHeader.split(" ")[1];
	}
}