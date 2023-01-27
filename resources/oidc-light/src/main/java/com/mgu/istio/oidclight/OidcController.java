package com.mgu.istio.oidclight;

import com.mgu.istio.oidclight.model.UserInfo;
import com.mgu.istio.oidclight.model.UserInformation;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class OidcController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcController.class);

    private SignatureConfiguration signatureConfiguration;
    private UserConfigurationService usersConfiguration;

    public OidcController(SignatureConfiguration signatureConfiguration, UserConfigurationService usersConfiguration) {
        this.signatureConfiguration = signatureConfiguration;
        this.usersConfiguration = usersConfiguration;
    }

    @GetMapping(path = "/.well-known/openid-configuration")
    public HashMap<String, String> openIdConfiguration(HttpServletRequest request) {
        LOGGER.info("Sending back openid configuration");
        String contextPath = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort()!=80 ) {
            contextPath += ":" + request.getServerPort();
        }
        HashMap<String, String> config = new HashMap<>();
        config.put("issuer", contextPath);
        config.put("jwks_uri", contextPath + "/.well-known/jwks.json");
        config.put("userinfo_endpoint", contextPath + "/idp/userinfo.openid");
        return config;
    }

    @GetMapping("/.well-known/jwks.json")
    public HashMap<String, List<JWKStructure>> getJwksKeys() {
        LOGGER.info("Sending back JWKS");
        HashMap<String, List<JWKStructure>> keys = new HashMap<>();
        keys.put("keys", Arrays.asList(getPublicJWKS()));
        return keys;
    }

    @GetMapping(path = "/idp/userinfo.openid", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfo> userinfo(@RequestHeader("Authorization") String auth) {
        LOGGER.debug("Sending back userinfo");

        String token = auth.substring("Bearer ".length());
        LOGGER.debug("Token is: {}", token);
        // https://stormpath.com/blog/jjwt-how-it-works-why
        // The JWT signature algorithm we will be using to sign the token

        String subject = Jwts.parser().setSigningKey(signatureConfiguration.getPrivateKey()).parseClaimsJws(token).getBody().getSubject();
        LOGGER.debug("/idp/userinfo.openid  invoked for user '{}'", subject);
        UserInformation userInfo = usersConfiguration.getUserInformation(subject);
        if (userInfo == null) {
            LOGGER.warn("user not found: '{}'", subject);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(new UserInfo(userInfo.getUserId(),
                "TODO",
                userInfo.getProfiles(),
                userInfo.getLastName(),
                userInfo.getFirstName(),
                userInfo.getEmail()), OK);
    }

    private JWKStructure getPublicJWKS() {
        RSAPublicKey key = signatureConfiguration.getPublicKey();
        RSAKey readableKey = (new RSAKey.Builder(key)).build();
        return new JWKStructure(SignatureConfiguration.JWT_ALGORITHM.getFamilyName(), readableKey.getKeyID(), "sig", readableKey.getModulus().toString(), readableKey.getPublicExponent().toString());
    }
}

class JWKStructure {
    public String kty;
    public String kid;
    public String use;
    public String n;
    public String e;

    public JWKStructure(String kty, String kid, String use, String n, String e) {
        this.kty = kty;
        this.kid = kid;
        this.use = use;
        this.n = n;
        this.e = e;
    }
}