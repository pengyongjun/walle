package com.amwalle.walle.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(locations={"classpath:/application-bean.xml"})
public class BeanConfig {
    @Bean
    public SpringInActionBean one() {
        return new SpringInActionBean();
    }

    @Bean
    public SpringInActionBean two() {
        return new SpringInActionBean();
    }
}
