package com.redhat.demo;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints
 * to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Override
        public void configure() throws Exception {
        // from("netty-http:proxy://0.0.0.0:8081")
        from("netty-http:proxy://0.0.0.0:8081")
            //.process(MySpringBootRouter::uppercase)
            .process(MySpringBootRouter::decryptMessage)
            .log("${headers.CamelHttpScheme}")
            .log("${headers.CamelHttpHost}")
            .log("${headers.CamelHttpPort}")
            .log("${headers.CamelHttpPath}")
            .toD("netty-http:"
                + "${headers." + Exchange.HTTP_SCHEME + "}://"
                + "${headers." + Exchange.HTTP_HOST + "}:"
                + "${headers." + Exchange.HTTP_PORT + "}"
                + "${headers." + Exchange.HTTP_PATH + "}")
            .process(MySpringBootRouter::uppercase);
    }

    public static void uppercase(final Exchange exchange) {
        final Message message = exchange.getIn();
        final String body = message.getBody(String.class);
        message.setBody(body.toUpperCase(Locale.US));
    }

    public static void decryptMessage(final Exchange exchange) throws Exception {
        final Message message = exchange.getIn();
        final String body = message.getBody(String.class);
        System.out.println("Recieved input from frontend :");
        System.out.println(body);
        RSAPrivateKey rsaPrivateKey = readPKCS8PrivateKey(new File("/mnt/secrets/pkcs8.key"));
        JWEObject jwe = JWEObject.parse(body);
        jwe.decrypt(new RSADecrypter(rsaPrivateKey));
        System.out.println("Decrypted Payload : " + jwe.getPayload().toString());
        message.setBody(jwe.getPayload().toString());
    }

    public static RSAPrivateKey readPKCS8PrivateKey(File file) throws Exception {
        String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
