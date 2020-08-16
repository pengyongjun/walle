package com.amwalle.walle.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
public class JSONValidatorController {
    private static final Logger logger = LoggerFactory.getLogger(JSONValidatorController.class);

    @RequestMapping(value = "/validator", method = RequestMethod.GET)
    public ModelAndView getValidator() {
        return new ModelAndView("/JSONValidator");
    }

    @RequestMapping(value = "/JSONValidator", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> validateJSON(@RequestBody String params) {
        logger.info(params);
        Map<String, String> result = new HashMap<>();
        result.put("result", "success");
        result.put("information", "validate passed!");
        return result;
    }

}
