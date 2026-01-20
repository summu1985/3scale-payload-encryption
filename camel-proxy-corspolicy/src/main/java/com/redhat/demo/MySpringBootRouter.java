package com.redhat.demo;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MySpringBootRouter extends RouteBuilder {

    private final String basePath =
        System.getenv().getOrDefault("BASE_PATH", "http://localhost:8182").trim();

    private final Set<String> allowedOrigins = Arrays.stream(
            System.getenv().getOrDefault("CORS_ALLOWED_ORIGINS", "").split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());

    private final boolean allowCredentials =
        Boolean.parseBoolean(System.getenv().getOrDefault("CORS_ALLOW_CREDENTIALS", "false"));

    private final String allowMethods =
        System.getenv().getOrDefault("CORS_ALLOW_METHODS", "GET,POST,PUT,DELETE,OPTIONS");

    private final String allowHeaders =
        System.getenv().getOrDefault("CORS_ALLOW_HEADERS",
            "Origin,Accept,X-Requested-With,Content-Type,Authorization," +
            "Access-Control-Request-Method,Access-Control-Request-Headers");

    @Override
    public void configure() {

        // Reverse proxy listener (NOT forward proxy)
        from("netty-http:http://0.0.0.0:8081?matchOnUriPrefix=true&httpMethodRestrict=GET,POST,PUT,DELETE,OPTIONS")
            .routeId("reverse-proxy-cors")

            .log("Incoming ${header.CamelHttpMethod} ${header.CamelHttpUri} Origin=${header.Origin}")

            // Compute CORS allow/deny once
            .process(e -> {
                String origin = e.getIn().getHeader("Origin", String.class);
                origin = (origin == null) ? null : origin.trim();
                boolean allowed = origin != null && allowedOrigins.contains(origin);

                e.setProperty("cors.origin", origin);
                e.setProperty("cors.allowed", allowed);
            })

            // Handle OPTIONS preflight locally
            .choice()
                .when(header(Exchange.HTTP_METHOD).isEqualTo("OPTIONS"))
                    .choice()
                        .when(simple("${exchangeProperty.cors.origin} != null && ${exchangeProperty.cors.allowed} == false"))
                            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(403))
                            .setBody(constant(""))
                            .stop()
                        .otherwise()
                            .process(this::addCorsHeadersIfAllowed)
                            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                            .setBody(constant(""))
                            .stop()
                    .end()
            .end()

            // Block disallowed origins for non-OPTIONS
            .choice()
                .when(simple("${exchangeProperty.cors.origin} != null && ${exchangeProperty.cors.allowed} == false"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(403))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .setBody(constant("{\"error\":\"CORS origin not allowed\"}"))
                    .stop()
            .end()

            // Build the outbound URL from BASE_PATH + HTTP_PATH (+ optional query)
            .process(e -> {
                String path = e.getIn().getHeader(Exchange.HTTP_PATH, String.class); // CamelHttpPath
                if (path == null || path.isBlank()) {
                    path = "/";
                }

                // Ensure basePath has no trailing slash (except "http://host/")
                String bp = basePath;
                while (bp.endsWith("/") && bp.length() > "http://x/".length()) {
                    bp = bp.substring(0, bp.length() - 1);
                }

                // Ensure path starts with '/'
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }

                String query = e.getIn().getHeader(Exchange.HTTP_QUERY, String.class); // CamelHttpQuery
                String target = (query == null || query.isBlank())
                    ? (bp)
                    : (bp + "?" + query);

                e.setProperty("proxy.targetUrl", target);
            })

            .log("Forwarding to ${exchangeProperty.proxy.targetUrl}")

            // Forward to dynamic destination
            .toD("${exchangeProperty.proxy.targetUrl}"
                + "?bridgeEndpoint=true&throwExceptionOnFailure=false")

            // Add CORS headers on the response (only if allowed)
            .process(this::addCorsHeadersIfAllowed);
    }

    private void addCorsHeadersIfAllowed(Exchange e) {
        String origin = (String) e.getProperty("cors.origin");
        Boolean allowed = (Boolean) e.getProperty("cors.allowed");

        if (origin == null || !Boolean.TRUE.equals(allowed)) return;

        e.getMessage().setHeader("Access-Control-Allow-Origin", origin);
        e.getMessage().setHeader("Vary", "Origin");
        e.getMessage().setHeader("Access-Control-Allow-Methods", allowMethods);
        e.getMessage().setHeader("Access-Control-Allow-Headers", allowHeaders);

        if (allowCredentials) {
            e.getMessage().setHeader("Access-Control-Allow-Credentials", "true");
        }

        e.getMessage().setHeader("Access-Control-Max-Age",
            System.getenv().getOrDefault("CORS_MAX_AGE", "600"));
    }
}
