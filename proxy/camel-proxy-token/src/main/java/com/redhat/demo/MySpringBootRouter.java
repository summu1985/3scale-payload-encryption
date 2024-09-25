package com.redhat.demo;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            .log("${headers.CamelHttpScheme}")
            .log("${headers.CamelHttpHost}")
            .log("${headers.CamelHttpPort}")
            .setHeader("originalPath", simple("${headers.CamelHttpPath}"))
            .setHeader("CamelHttpPath", constant("/token"))
            .log("${headers.CamelHttpPath}")
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, simple("application/json"))
            .to("http://localhost:8181?bridgeEndpoint=true")
            .to("log:DEBUG?showBody=true&showHeaders=true")
            .convertBodyTo(String.class)
            .log("${body}")
            .process(MySpringBootRouter::retrieveToken)
            .to("log:DEBUG?showHeaders=true")
            // restore the original path
            .setHeader("CamelHttpPath", simple("${headers.originalPath}"))
            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
            .toD("netty-http:"
                + "${headers." + Exchange.HTTP_SCHEME + "}://"
                + "${headers." + Exchange.HTTP_HOST + "}:"
                + "${headers." + Exchange.HTTP_PORT + "}"
                + "${headers." + Exchange.HTTP_PATH + "}");
            // .process(MySpringBootRouter::uppercase);
    }

    public static void retrieveToken(final Exchange exchange) throws Exception {
        final Message message = exchange.getIn();
        final String body = message.getBody(String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(body, new TypeReference<>() {
        });
        String token = map.get("access_token").toString();
        System.out.println("Token = " + token);
        exchange.getIn().setHeader("Authorization", "Bearer "+token);
    }
}
