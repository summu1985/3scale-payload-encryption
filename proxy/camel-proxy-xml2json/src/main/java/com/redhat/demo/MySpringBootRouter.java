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
            .to("xj:identity?transformDirection=XML2JSON")
            .log("xml to json : ${body}")
            .setHeader(Exchange.CONTENT_TYPE, simple("application/json"))
            //.process(MySpringBootRouter::decryptMessage)
            .log("${headers.CamelHttpScheme}")
            .log("${headers.CamelHttpHost}")
            .log("${headers.CamelHttpPort}")
            .log("${headers.CamelHttpPath}")
            .log("${headers.Content-Type}")
            .toD("netty-http:"
                + "${headers." + Exchange.HTTP_SCHEME + "}://"
                + "${headers." + Exchange.HTTP_HOST + "}:"
                + "${headers." + Exchange.HTTP_PORT + "}"
                + "${headers." + Exchange.HTTP_PATH + "}")
            .to("xj:json2xml-lead.xsl?transformDirection=JSON2XML")
            .log("json to xml : ${body}")
            .setHeader(Exchange.CONTENT_TYPE, simple("application/xml"));
    }

    // public static void uppercase(final Exchange exchange) {
    //     final Message message = exchange.getIn();
    //     final String body = message.getBody(String.class);
    //     message.setBody(body.toUpperCase(Locale.US));
    // }

    // public static void xml2json(final Exchange exchange) {
    //     final Message message = exchange.getIn();
    //     final String body = message.getBody(String.class);
    //     message.setBody(body.toUpperCase(Locale.US));
    // }

}
