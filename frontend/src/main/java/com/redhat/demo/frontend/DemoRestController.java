package com.redhat.demo.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.JWK;

@RestController
public class DemoRestController {

    org.slf4j.Logger logger = LoggerFactory.getLogger(DemoRestController.class);

    @RequestMapping(value = "/greeting", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String greetInResponse(@RequestBody String greeting) throws IOException {
        String data = "";
        JWEAlgorithm alg = JWEAlgorithm.RSA_OAEP_256;
        EncryptionMethod enc = EncryptionMethod.A256GCM;

        String pubKeyContent = new String(Files.readAllBytes(new File("/mnt/secrets/publickey.crt").toPath()), Charset.defaultCharset());

    String publicKeyPEM = pubKeyContent
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replaceAll(System.lineSeparator(), "")
      .replace("-----END PUBLIC KEY-----", "");

    //   Base64 b64 = new Base64();
    //   byte [] decoded = b64.decode(publicKeyPEM);
    // byte[] encoded = new Base64().decode(publicKeyPEM);
    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

    KeyFactory keyFactory = null;
    try {
        keyFactory = KeyFactory.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    RSAPublicKey rsaPublicKey = null;
    try {
        rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
    } catch (InvalidKeySpecException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

        // Generate an RSA key pair
        //KeyPairGenerator rsaGen;
        try {
            //rsaGen = KeyPairGenerator.getInstance("RSA");
            //rsaGen.initialize(2048);
            //KeyPair rsaKeyPair = rsaGen.generateKeyPair();
            //RSAKey pubRSA = (RSAKey) PemUtils.readPublicKeyFromFile("/path/to/rsa/key.pem", "RSA"));
            //RSAPublicKey rsaPublicKey = (RSAPublicKey) rsaKeyPair.getPublic();
            //RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();

            System.out.println("public key = " + rsaPublicKey.toString());

            // Generate the Content Encryption Key (CEK)
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(enc.cekBitLength());
            SecretKey cek = keyGenerator.generateKey();

            // Encrypt the JWE with the RSA public key + specified AES CEK
            JWEObject jwe = new JWEObject(
                    new JWEHeader(alg, enc),
                    new Payload(greeting));
            try {
                jwe.encrypt(new RSAEncrypter(rsaPublicKey, cek));
                String jweString = jwe.serialize();
                System.err.println("encrypted data : " + jweString);
                // StringBuilder jsonData = new StringBuilder();
                // data = "{ \"header\":"
                //         + jwe.getHeader()
                //         + ",\"encrypted-key\":\""
                //         + jwe.getEncryptedKey()
                //         + "\",\"encrypted-text\":\""
                //         + jwe.getCipherText()
                //         + "\",\"iv\":\""
                //         + jwe.getIV()
                //         + "\",\"tag\":\""
                //         + jwe.getAuthTag()
                //         + "\"}";

                // System.out.println("Data : " + data);

                // Now send this request to the backend

                RestTemplate restTemplate = new RestTemplate();
                String fooResourceUrl = "https://demo-3scale-apicast-staging.apps.cluster-slsjz.slsjz.sandbox374.opentlc.com:443/greeting?user_key=5beee0a6b098664d383cb5a97e45c51c";
                ResponseEntity<String> response = restTemplate.postForEntity(fooResourceUrl , jweString , String.class);
                if (response.getStatusCode().equals(HttpStatus.OK)){
                    data = response.getBody();
                    System.out.println("Got response :" + data);
                } else {
                    System.err.println("Something went wrong: "+response.toString());
                }

            } catch (JOSEException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    
        String responseGreeting = "pong";
        return data;
    }

//     public  RSAPublicKey readX509PublicKey(File file) throws Exception {
//     String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

//     String publicKeyPEM = key
//       .replace("-----BEGIN PUBLIC KEY-----", "")
//       .replaceAll(System.lineSeparator(), "")
//       .replace("-----END PUBLIC KEY-----", "");

//       Base64 b64 = new Base64();
//       byte [] decoded = b64.decode(publicKeyPEM);
//     byte[] encoded = new Base64().decode(publicKeyPEM);
//     //byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

//     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//     X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
//     return (RSAPublicKey) keyFactory.generatePublic(keySpec);
// }

// public RSAPublicKey getPublicKey(File privateKeyFile, File publicKeyFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
//             //String privateKeyContent = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("private_key_pkcs8.pem").toURI())));
//         String privateKeyContent = new String(Files.readAllBytes(privateKeyFile.toPath()), Charset.defaultCharset());
//         String publicKeyContent = new String(Files.readAllBytes(publicKeyFile.toPath()), Charset.defaultCharset());

//         JWK.load(null, publicKeyContent, null)
//         privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "").replace("-----END ENCRYPTED PRIVATE KEY-----", "");
//         publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "");;

//         KeyFactory kf = KeyFactory.getInstance("RSA");

//         PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
//         PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);

//         X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
//         RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

//         System.out.println("Private key: " + privKey);
//         System.out.println("Public key: " + pubKey);

//         return pubKey;
// }
}