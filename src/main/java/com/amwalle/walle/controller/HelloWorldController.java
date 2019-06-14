package com.amwalle.walle.controller;

import com.amwalle.walle.scheduler.WalleJobManager;
import com.amwalle.walle.scheduler.WalleJob;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@RestController
@EnableAutoConfiguration
public class HelloWorldController {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldController.class);

    @Autowired
    private WalleJobManager jobManager;

    @RequestMapping(value = "/schedule", method = RequestMethod.GET)
    public String setSchedule() {
        logger.info("--------test-------");
        try {
            jobManager.startJob("0 0/1 * * * ?", "TestJob1", "TestJobGroup1", WalleJob.class);
            jobManager.triggerJob("TestJob1", "TestJobGroup1");
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
        }

        return "hello world";
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView index(Model model) {
        logger.info(">>>>>>>>>>>> index >>>>>>>>>>>");
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ModelAndView test(Model model) {
        logger.info(">>>>>>>>>>>> test >>>>>>>>>>>");
        return new ModelAndView("test");
    }

    @RequestMapping(value = "/pathVariable/{test}", method = RequestMethod.GET)
    public ModelAndView pathVariable(@PathVariable String test, Model model) {
        logger.info(test);
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView home(Model model) {
        logger.info(">>>>>>>>>>>> home >>>>>>>>>>>");
        return new ModelAndView("home");
    }

    @RequestMapping(value = "/testhello", method = RequestMethod.GET)
    public ModelAndView vm(Model model) {
        logger.info(" >>>>>>>>>>>>>> hello >>>>>>>>>>>>");
        model.addAttribute("hello", "world");
        return new ModelAndView("hello");
    }
}
