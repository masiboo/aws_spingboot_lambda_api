package org.iprosoft.trademarks.aws.artefacts.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JWTUtils {

	public static boolean verifyToken(final String authToken, final String secretKey) {
		boolean isVerified = false;
		try {
			Algorithm algorithm = Algorithm.HMAC256(secretKey);
			JWTVerifier jwtVerifier = JWT.require(algorithm).build();
			DecodedJWT verifiedJWT = jwtVerifier.verify(authToken);
			verifiedJWT.getClaims().forEach((k, v) -> log.info(k + " :: " + v.asString()));
			isVerified = true;
		}
		catch (JWTVerificationException e) {
			log.error("Exception while validating the JWT Singature", e);
		}
		return isVerified;
	}

}
