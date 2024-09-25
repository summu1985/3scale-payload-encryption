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

    //@PostMapping("/greeting")
    @PostMapping(path = "/token",
   consumes = MediaType.APPLICATION_JSON_VALUE,
   produces = MediaType.APPLICATION_JSON_VALUE)
    public String greetInResponse(@RequestBody String leadRequest) throws Exception {

        logger.info("[ Token-backend ] Recieved token request: " + leadRequest);
        // // Convert JSON string to Map
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(leadRequest, new TypeReference<>() {
        });
        //map.put("uid", "1234");
        map.put("access_token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6MTF9.eyJqdGkiOiJodHRwczovL3RyYWtwYXRjaC5tYW5pcGFsaG9zcGl0YWxzLmNvbS9vYXV0aDIuTDlxTzAwZDg3dXUyYVZWRGFHcUVLNnEtNE1FIiwiaXNzIjoiaHR0cHM6Ly90cmFrcGF0Y2gubWFuaXBhbGhvc3BpdGFscy5jb20vb2F1dGgyIiwic3ViIjoiUGF0aWVudEFQUCIsImV4cCI6MTcyNzA5NzgzNiwiYXVkIjoiV0FvWG8yMmVvbGVKZnByRkl5N25BdWZ6WFFXSjhCZ0hTcVpjUDNyWVljayJ9.pXRWeF7cdgnqJXT0y623JTzpX2nWPD3qa1iA9oOI5R0pvsJU4lGJ9blTcqybmD483aFVd-_s0Dd8EJNmIU82PDq4KmE4rGkKqlZFzw0OEm1UqgvjnhQMuEDifmTNaDwoUSeLvCvLSA1WaS_x5OWrgk9I8kBwH5rVErRXCaFt81fmNCGSphe70Tt2dVRUYgWauU9g95x0XwfsJIbaakKpDgaIBULEF9qnUOyJY3wTacCieaQRr0w78lnuhG430xLrkZrcgJQ6qUfuceeu6VeNdCXMhSKy20XxD2BrJfHuWS8NZ-FYcDK_AjEpQ50Uu4Aqq3PNsCSBw3l3Btj-4A06Wg");
        map.put("token_type", "Bearer");
        map.put("token_expiry_date_time", "2024-09-23 18:53:56");
        map.put("message", "Success");

        String responseData = mapper.writeValueAsString(map);

        logger.info("[ Token-backend ] Sending token data : " + responseData);
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
