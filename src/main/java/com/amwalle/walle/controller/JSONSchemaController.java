package com.amwalle.walle.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.amwalle.walle.util.JSONNode;
import com.amwalle.walle.util.JSONTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class JSONSchemaController {
    private static final Logger logger = LoggerFactory.getLogger(JSONSchemaController.class);

    @RequestMapping(value = "/schema", method = RequestMethod.GET)
    public ModelAndView jsonSchema() {
        return new ModelAndView("schema");
    }

    @RequestMapping(value = "/change", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> change(String test) {
        Map<String, Object> resultMap = new HashMap<>();
        System.out.println("change");
        System.out.println("test: " + test);
        if (null == test) {
            resultMap.put("result", "参数不合法!");
            return resultMap;
        }

        resultMap.put("result", "SUCCESS");
        resultMap.put("hello", "hello, " + test);
        return resultMap;
    }

    @RequestMapping(value = "/analysisSchema", method = RequestMethod.POST)
    @ResponseBody
    public List<JSONNode> analysisSchema(String json) {
        logger.info("json: " + json);

        JSONObject jsonObject = JSONObject.parseObject(json, JSONObject.class, Feature.OrderedField);

        logger.info(JSON.toJSONString(jsonObject, true));

        JSONNode root = JSONTree.createDeduplicateJSONTree(jsonObject, "root", "#", 0);
        return JSONTree.depthFirstTraversal(root);
    }
}
