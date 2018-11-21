package com.amwalle.walle.controller;

import com.amwalle.walle.raspi.camera.WebCamera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@EnableAutoConfiguration
public class HelloWorldController {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldController.class);

    @Autowired
    private WebCamera webCamera;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String helloWorld() {
        logger.info("--------test-------");

        try {
            webCamera.getVideoStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "hello world";
    }
}
