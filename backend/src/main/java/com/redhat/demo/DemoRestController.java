package com.redhat.demo;

import java.io.File;
import java.util.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;

@RestController
public class DemoRestController {

    org.slf4j.Logger logger = LoggerFactory.getLogger(DemoRestController.class);

    @PostMapping("/greeting")
    public String greetInResponse(@RequestBody String greeting) throws Exception {

        logger.info("Recieved greeting: " + greeting);
        // String privKeyContent = new String(Files.readAllBytes(new
        // File("/Users/sumit/pkcs8.key").toPath()),
        // Charset.defaultCharset());
        // String pemEncodedRSAPublicKey = "-----BEGIN PUBLIC KEY-----
        // RSAPrivateKey rsaPrivateKey = readPKCS8PrivateKey(new File("/mnt/secrets/pkcs8.key"));
        // JWEObject jwe = JWEObject.parse(greeting);
        // jwe.decrypt(new RSADecrypter(rsaPrivateKey));
        // System.out.println("Decrypted Payload : " + jwe.getPayload().toString());
        // assertEquals("Hello, world!", jwe.getPayload().toString());
        // ObjectMapper mapper = new ObjectMapper();

        // // convert JSON string to Map
        // // uncheck assignment
        // // Map<String, Object> map = mapper.readValue(json, Map.class);

        // // Convert JSON string to Map
        // Map<String, Object> map = mapper.readValue(greeting, new TypeReference<>()
        // {});

        // // Output the contents of the Map to verify the conversion
        // System.out.println("Map content: " + map);
        // String encodedHeadeString =
        // Base64.getEncoder().encodeToString(map.get("header").toString().getBytes());
        // System.out.println("Base64 header : " + encodedHeadeString);
        // String responseGreeting = jwe.getPayload().toString();
        // // Convert JSON string to Map
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(greeting, new TypeReference<>() {
        });
        map.put("uid", "1234");
        map.put("productCategory", "Home Loan");
        map.put("sourcingChannel", "Web");
        map.put("mobileNumber", "9051933399");
        map.put("partnerName", "AIG");
        map.put("leadId", "SFDC-1");
        map.put("applicantFirstName", "Sumit");
        map.put("applicantMiddleName", "Sibatosh");
        map.put("applicantLastName", "Mukherjee");
        map.put("applicantDOB", "21-04-85");
        map.put("applicantPanNumber", "AVPMOH3772I");

        String responseData = mapper.writeValueAsString(map);
        return responseData;
    }

    // public RSAPrivateKey readPKCS8PrivateKey(File file) throws Exception {
    //     String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

    //     String privateKeyPEM = key
    //             .replace("-----BEGIN PRIVATE KEY-----", "")
    //             .replaceAll(System.lineSeparator(), "")
    //             .replace("-----END PRIVATE KEY-----", "");

    //     byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

    //     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    //     PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    //     return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    // }
}
