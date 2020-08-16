package com.amwalle.walle.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SpringInActionBean {
    private static final Logger logger = LoggerFactory.getLogger(SpringInActionBean.class);

    public SpringInActionBean() {
        logger.info("---我被调用了----");
    }

    public void sayHi(String name) {
        logger.info("Hi, " + name);
    }
}
