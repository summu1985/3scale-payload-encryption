package com.redhat.demo;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoRestController {

    org.slf4j.Logger logger = LoggerFactory.getLogger(DemoRestController.class);

    @PostMapping("/greeting")
    public String greetInResponse(@RequestBody String greeting) {

        logger.info("Recieved greeting: " + greeting);
        String responseGreeting = "pong";
        return responseGreeting;
    }

}
