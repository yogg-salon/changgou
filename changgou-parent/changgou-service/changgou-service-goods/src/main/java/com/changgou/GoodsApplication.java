package com.changgou;

import entity.IdWorker;
import entity.MyFeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@EnableEurekaClient //开启Eureka客户端
@MapperScan(basePackages="com.changgou.goods.dao")
/**
 * 注解包名tk.mybatis.spring.annotation.MapperScan;
 */
public class GoodsApplication {
    public static void main(String[] args) {

        SpringApplication.run(GoodsApplication.class,args);
    }
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,0);
    }
    @Bean
    public MyFeignInterceptor MyFeignInterceptor(){
        return new MyFeignInterceptor();
    }
}

