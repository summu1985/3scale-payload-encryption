package com.redhat.demo;

import java.io.File;
import java.util.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.nimbusds.jose.JWEObject;
// import com.nimbusds.jose.crypto.RSADecrypter;

@RestController
public class DemoRestController {

    org.slf4j.Logger logger = LoggerFactory.getLogger(DemoRestController.class);

    //@PostMapping("/greeting")
    @GetMapping(path = "/user",
   //consumes = MediaType.APPLICATION_JSON_VALUE,
   produces = MediaType.APPLICATION_JSON_VALUE)
    // public String greetInResponse(@RequestBody String userRequest) throws Exception {
    public String greetInResponse() throws Exception {

        logger.info("[User-backend] Recieved user request: ");
        // // Convert JSON string to Map
        ObjectMapper mapper = new ObjectMapper();
        String userRequest = "{\"uuid\":\"1234\"}";
        Map<String, Object> map = mapper.readValue(userRequest, new TypeReference<>() {
        });
        //map.put("uid", "1234");
        map.put("userName", "srk");
        map.put("firstName", "Shahrukh");
        map.put("lastName", "Khan");
        map.put("mobileNumber", "1234567890");
        map.put("status", "active");
        // map.put("city", "Mumbai");
        // map.put("applicantMiddleName", "Sibatosh");
        // map.put("applicantLastName", "Mukherjee");
        // map.put("applicantDOB", "21-04-85");
        // map.put("applicantPanNumber", "AVPMOH3772I");

        String responseData = mapper.writeValueAsString(map);
        logger.info("[User-backend] Sending user data : " + responseData);
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
