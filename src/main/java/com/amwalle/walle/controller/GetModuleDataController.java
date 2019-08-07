package com.amwalle.walle.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
public class GetModuleDataController {
    private static final Logger logger = LoggerFactory.getLogger(GetModuleDataController.class);

    @RequestMapping(value = "/moduleData", method = RequestMethod.GET)
    public ModelAndView getValidator() {
        return new ModelAndView("/getModuleData");
    }

    @RequestMapping(value = "/doReformat", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> reformatModuleData(@RequestBody String params) {
        logger.info(params);
        JSONObject paramObject = JSON.parseObject(params, JSONObject.class, Feature.OrderedField);
        String moduleId = (String) paramObject.get("moduleId");
        String ossData = (String) paramObject.get("ossData");

        Map<String, String> result = new HashMap<>();
        if (StringUtils.isEmpty(ossData) || !ossData.startsWith("[")) {
            result.put("result", "failed");
            result.put("message", "OSS data is not correct, please check!");
            return result;
        }

        JSONArray ossDataArray = JSON.parseObject(ossData, JSONArray.class, Feature.OrderedField);

        result.put("result", "success");
        result.put("message", getModuleData(ossDataArray, moduleId).toJSONString());
        return result;
    }

    private JSONObject getModuleData(JSONArray ossData, String moduleId) {
        JSONObject moduleData = JSON.parseObject("{}", JSONObject.class, Feature.OrderedField);

        for (int index = 0, length = ossData.size(); index < length; index++) {
            JSONObject moduleObject = ossData.getJSONObject(index);
            if (moduleId.equals(moduleObject.getString("moduleId"))) {
                JSONObject datas = JSON.parseObject("{}", JSONObject.class, Feature.OrderedField);
                datas.fluentPut("datas", moduleObject.get("datas"));
                moduleData.fluentPut(moduleObject.getString("tagId"), datas);
            }
        }

        return moduleData;
    }

}
