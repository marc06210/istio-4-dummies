package com.mgu.istio.oidclight;

import static java.nio.file.Files.createTempFile;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Bean in charge of reading the key files and to expose public and private keys used
 * to sign or validate the JWT.<br/>
 * This bean is activated when in full Ping mode.
 * @author m408461
 *
 */
@Component
public class SignatureConfiguration {

    public static final SignatureAlgorithm JWT_ALGORITHM = SignatureAlgorithm.RS512;

    private Key privateKey = null;
    private RSAPublicKey publicKey = null;

    public Key getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPrivateKey(Key privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @PostConstruct
    public void createKeys() throws Exception {
        try {
            // TODO: see if we can't make it simpler and/or use same code for public and private
            ClassPathResource publicKeyFile = new ClassPathResource("public_key.der");
            Path tempFile = createTempFile("ss4h_public_key", ".der");
            copyInputStreamToFile(publicKeyFile.getInputStream(), tempFile.toFile());
            File file1 = tempFile.toFile();

//            File file1 = ResourceUtils.getFile("classpath:public_key.der");
            FileInputStream fis1 = new FileInputStream(file1);
            DataInputStream dis1 = new DataInputStream(fis1);
            byte[] keyBytes1 = new byte[(int) file1.length()];
            dis1.readFully(keyBytes1);
            dis1.close();

            X509EncodedKeySpec spec1 = new X509EncodedKeySpec(keyBytes1);
            KeyFactory kf1 = KeyFactory.getInstance("RSA");
            publicKey = (RSAPublicKey) kf1.generatePublic(spec1);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw e;
        }
        try {
            ClassPathResource privateKeyFile = new ClassPathResource("private_key.der");
            Path tempFile = createTempFile("ss4h_private_key", ".der");
            copyInputStreamToFile(privateKeyFile.getInputStream(), tempFile.toFile());
            File file = tempFile.toFile();

//            File file = ResourceUtils.getFile("classpath*:private_key.der");
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);

            byte[] keyBytes = new byte[(int) file.length()];
            dis.readFully(keyBytes);
            dis.close();

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

}

// here principle to generate public and private keys
// 1 - Generate a 2048-bit RSA private key
// openssl genrsa -out private_key.pem 2048
// 2 - Convert private Key to PKCS#8 format (so Java can read it)
// openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
// 3 - Output public key portion in DER format (so Java can read it)
// openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der

// here to validate a token manually https://www.baeldung.com/java-jwt-token-decode



