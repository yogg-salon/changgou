package com.changgou;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author yanming
 * @version 1.0 2021/1/15
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableEurekaClient
@EnableFeignClients
public class WeixinPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeixinPayApplication.class,args);
    }
    @Autowired
    private Environment env;
    /***
     * 创建DirectExchange交换机
     * @return
     */
    public DirectExchange basicExchange(){
        return new DirectExchange(env.getProperty("mq.pay.queue.order"),true,false);
    }

    /**
     * 创建队列
     * @return
     */
    @Bean(name="queueOrder")
    public Queue queueOrder(){
        return new Queue(env.getProperty("mq.pay.queue.order"),true);
    }
    @Bean
    public Binding basicBindingOrder(){
        return BindingBuilder.bind(
                queueOrder())
                .to(basicExchange())
                .with(env.getProperty("mq.pay.queue.order"));
    }

    /**
     * 创建秒杀队列
     * @return
     */
    @Bean
    public Queue queueSeckillOrder(){
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"),true);
    }


    /**
     * 将秒杀队列绑定到秒杀交换机上
     * @return
     */
    @Bean
    public Binding basicBindingSeckillOrder() {
        return BindingBuilder
                .bind(queueSeckillOrder())
                .to(basicExchange())
                .with(env.getProperty("mq.pay.routing.seckillorderkey"));
    }
    /**
     * 到期数据队列
     * @return
     */
    @Bean
    public Queue seckillOrderTimerQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillordertimer"), true);
    }

    /**
     * 超时数据队列
     * @return
     */
    @Bean
    public Queue delaySeckillOrderTimerQueue() {
    return QueueBuilder.durable(env.getProperty("mq.pay.queue.seckillordertimerdelay"))
            .withArgument("x-dead-letter-exchange", env.getProperty("mq.pay.exchange.order"))        // 消息超时进入死信队列，绑定死信队列交换机
            .withArgument("x-dead-letter-routing-key", env.getProperty("mq.pay.queue.seckillordertimer"))   // 绑定指定的routing-key
            .build();
    }

    /***
     * 交换机与队列绑定
     * @return
     */
    @Bean
    public Binding basicBinding() {
    return BindingBuilder.bind(seckillOrderTimerQueue())
            .to(basicExchange())
            .with(env.getProperty("mq.pay.queue.seckillordertimer"));
    }
}
