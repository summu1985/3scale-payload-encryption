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

            // onException(org.apache.camel.processor.ThrottlerRejectedExecutionException.class)
            // .process(MySpringBootRouter::throttlingExceptionHandler)
            // .handled(true)
            // .setHeader(Exchange.CONTENT_TYPE, simple("application/json"))
            // .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(429))
            // .log("rate limit exceeded");

            onException(org.apache.camel.component.jsonvalidator.JsonValidationException.class)
            .process(MySpringBootRouter::validationExceptionHandler)
            .handled(true)
            .setHeader(Exchange.CONTENT_TYPE, simple("application/json"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
            .log(Exchange.EXCEPTION_CAUGHT);
            

        // from("netty-http:proxy://0.0.0.0:8081")
        from("netty-http:proxy://0.0.0.0:8081")
            // .log("throttling proxy : ${body}")
            .to("log:DEBUG?showBody=true&showHeaders=true")
            //.process(MySpringBootRouter::throttle)
            .convertBodyTo(String.class)
            .setHeader("myBody", body())
            .choice()
                .when(simple("${headers.Query} == 'lead'"))
                    .to("json-validator:lead-schema.json")
                    .to("direct:process")
                .endChoice()
                // .when(simple("${headers.Source} == 'app'"))
                //     .throttle(6)
                //     .timePeriodMillis(30000)    // Time period is 0.5 minute = 30000 ms
                //     .totalRequestsMode()
                //     //.concurrentRequestsMode()
                //     .rejectExecution(true)
                //     .to("direct:process")
                // .endChoice()
                .otherwise()
                    // .throttle(1)
                    // .timePeriodMillis(30000)    // Time period is 0.5 minute = 30000 ms
                    // .totalRequestsMode()
                    // //.concurrentRequestsMode()
                    // .rejectExecution(true)
                    .to("direct:process")
                .endChoice()
            .end();

        from("direct:process")
        .setBody(simple("${headers.myBody}"))
        .log("${headers.CamelHttpScheme}")
        .log("${headers.CamelHttpHost}")
        .log("${headers.CamelHttpPort}")
        .log("${headers.CamelHttpPath}")
        .log("${headers.Content-Type}")
        .to("log:DEBUG?showBody=true&showHeaders=true")
        .toD("netty-http:"
            + "${headers." + Exchange.HTTP_SCHEME + "}://"
            + "${headers." + Exchange.HTTP_HOST + "}:"
            + "${headers." + Exchange.HTTP_PORT + "}"
            + "${headers." + Exchange.HTTP_PATH + "}")
        // .to("xj:json2xml-lead.xsl?transformDirection=JSON2XML")
        .log("Response from downstream: ${body}");
        // .setHeader(Exchange.CONTENT_TYPE, simple("application/xml"));
    }

    // public static void throttlingExceptionHandler(final Exchange exchange) {
    //     final Message message = exchange.getIn();
    //     //final String body = message.getBody(String.class);
    //     message.setBody("{\"error\":\"Usage limit exceeded\"}");
    // }
    public static void validationExceptionHandler(final Exchange exchange) {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        final Message message = exchange.getIn();
        //final String body = message.getBody(String.class);
        message.setBody("{\"exception\":\""+cause.getMessage().toString()+"\"}");
    }

    // public static void uppercase(final Exchange exchange) {
    //     final Message message = exchange.getIn();
    //     final String body = message.getBody(String.class);
    //     message.setBody(body.toUpperCase(Locale.US));
    // }

    // public static void throttle(final Exchange exchange) {
    //     final Message message = exchange.getIn();
    //     final String body = message.getBody(String.class);
    //     message.setBody(body.toUpperCase(Locale.US));
    // }

}
