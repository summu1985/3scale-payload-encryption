package com.redhat.demo;

import java.util.Locale;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Override
        public void configure() throws Exception {
        from("netty-http:proxy://0.0.0.0:8081")
            .process(MySpringBootRouter::uppercase)
            .log("${headers.CamelHttpScheme}")
            .log("${headers.CamelHttpHost}")
            .log("${headers.CamelHttpPort}")
            .log("${headers.CamelHttpPath}")
            .toD("netty-http:"
                + "${headers." + Exchange.HTTP_SCHEME + "}://"
                + "${headers." + Exchange.HTTP_HOST + "}:"
                + "${headers." + Exchange.HTTP_PORT + "}"
                + "${headers." + Exchange.HTTP_PATH + "}")
            // .to("netty-http://http://backend-summukhe-dev.apps.sandbox-m4.g2pi.p1.openshiftapps.com:80/greeting")
            .process(MySpringBootRouter::uppercase);
    }

    public static void uppercase(final Exchange exchange) {
        final Message message = exchange.getIn();
        final String body = message.getBody(String.class);
        message.setBody(body.toUpperCase(Locale.US));
    }

}
