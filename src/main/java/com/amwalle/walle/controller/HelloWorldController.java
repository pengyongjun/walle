package com.amwalle.walle.controller;

import com.amwalle.walle.scheduler.WalleJobManager;
import com.amwalle.walle.scheduler.WalleJob;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class HelloWorldController {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldController.class);

    @Autowired
    private WalleJobManager jobManager;

    @RequestMapping(value = "/walle", method = RequestMethod.GET)
    public String helloWorld() {
        logger.info("--------test-------");
//        Mail.sendMail("test","test", "yongjun.peng@lazada.com");
        try {
            jobManager.startJob("0 0/1 * * * ?", "TestJob1", "TestJobGroup1", WalleJob.class);
//            jobManager.resetJobCron("TestJob1","TestJobGroup1","0 0/2 * * * ?");
            jobManager.triggerJob("TestJob1","TestJobGroup1");
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
        }

        return "hello world";
    }
}
