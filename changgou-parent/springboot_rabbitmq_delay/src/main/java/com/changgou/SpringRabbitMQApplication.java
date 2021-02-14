package com.changgou;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yanming
 * @version 1.0 2021/1/19
 */
@SpringBootApplication
@EnableRabbit
public class SpringRabbitMQApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringRabbitMQApplication.class,args);
    }
}
