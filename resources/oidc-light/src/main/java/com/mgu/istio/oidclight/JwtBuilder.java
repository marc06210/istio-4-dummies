package com.mgu.istio.oidclight;

import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.*;

/**
 * Convenient class to hold static methods dealing with a JWT.
 *
 * @author m408461
 *
 */
public class JwtBuilder {


    public static final String JWT_ID = "mgu-oidc-light";
    public static final String SCOPE_KEY = "scope";
    public static final List<String> PING_JWT_SCOPES = Arrays.asList("openid", "address", "email", "phone", "profile");

    /**
     * Creates a JWT.
     *
     * @param subject - user concerned by the JWT
     * @param issuer - URL of the issuer to be injected in the JWT
     * @param jwtTtl - TTL of the JWT
     * @param signingKey - the private key used to sign the JWT
     * @return
     */
    public static String createJWT(String subject, String issuer, int jwtTtl, Key signingKey, List<String> profiles) {
        Calendar c = Calendar.getInstance();
        // Let's set the JWT Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put(SCOPE_KEY, PING_JWT_SCOPES);
        HashMap<String, String> roles = new HashMap<>();
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setId(JWT_ID)
                .setIssuedAt(c.getTime())
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(SignatureConfiguration.JWT_ALGORITHM, signingKey);

        c.add(Calendar.SECOND, jwtTtl);
        builder.setExpiration(c.getTime());

        // Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

}

class Auth {
    String name;
    String value;
    public Auth(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
