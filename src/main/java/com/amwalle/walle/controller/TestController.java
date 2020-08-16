package com.amwalle.walle.controller;

import com.amwalle.walle.bean.SpringInActionBean;
import com.amwalle.walle.scheduler.WalleJob;
import com.amwalle.walle.scheduler.WalleJobManager;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableAutoConfiguration
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private WalleJobManager jobManager;

    @Autowired
    SpringInActionBean springInActionBean;

    @RequestMapping(value = "/schedule", method = RequestMethod.GET)
    public String setSchedule() {


        springInActionBean.sayHi("world");
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
        ModelAndView modelAndView = new ModelAndView("hello");
        model.addAttribute("test", "world");
        return modelAndView;
    }

    @RequestMapping("/ats/callback")
    public void atsTask(HttpServletRequest request, HttpServletResponse response) {
        logger.info(String.valueOf(request.getParameterMap()));
    }
}
