package com.yan.changgou.item.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yanming
 * @version 1.0 2020/12/26
 */
@ControllerAdvice
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {
    /**
     * 静态资源放行
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/items/**").
                addResourceLocations("classpath:/templates/items/");
    }
}
