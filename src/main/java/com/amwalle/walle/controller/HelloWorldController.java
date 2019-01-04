package com.amwalle.walle.controller;

import com.amwalle.walle.util.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class HelloWorldController {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldController.class);

    @RequestMapping(value = "/walle", method = RequestMethod.GET)
    public String helloWorld() {
        logger.info("--------test-------");

        Mail.sendMail("test","test", "yongjun.peng@lazada.com");

        return "hello world";
    }
}
