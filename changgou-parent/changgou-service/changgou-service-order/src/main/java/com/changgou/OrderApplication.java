package com.changgou;

import entity.IdWorker;
import entity.MyFeignInterceptor;
import entity.TokenDecode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author yanming
 * @version 1.0 2020/12/30
 */
@SpringBootApplication

@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.changgou.goods.feign","com.changgou.user.feign"})
@MapperScan(basePackages = {"com.changgou.order.dao"})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,1);
    }
    @Bean
    public TokenDecode tokenDecode(){

        return  new TokenDecode();
    }
    @Bean
    public MyFeignInterceptor MyFeignInterceptor(){
        return new MyFeignInterceptor();
    }

}
