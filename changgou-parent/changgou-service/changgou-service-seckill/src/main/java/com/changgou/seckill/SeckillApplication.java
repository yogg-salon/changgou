package com.changgou.seckill;

import entity.IdWorker;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author yanming
 * @version 1.0 2021/1/18
 */

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.changgou.seckill.feign"})
@MapperScan(basePackages = {"com.changgou.seckill.dao"})
@EnableScheduling
@EnableAsync
public class SeckillApplication {






    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class,args);
    }
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(1,1);
    }


    @Autowired
    private Environment env;
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        //采用普通的key 为 字符串
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }
    /**
     * 创建DirectExchange交换机
     * @return
     */
    @Bean
    public DirectExchange basicExchange(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"),true,false);

    }

    /**
     * 创建订单队列
     * @return
     */

    @Bean(name="queueOrder")
    public Queue queueOrder(){
        return new Queue(env.getProperty("mq.pay.queue.order"),true);
    }
    /**
     * 创建秒杀队列
     * @return
     */
    @Bean(name="queueSeckillOrder")
    public Queue queueSeckillOrder(){
        return  new Queue(env.getProperty("mq.pay.queue.seckillorder"),true);
    }

    /**
     * 将队列绑定到交换机上
     * @return
     */
    @Bean
    public Binding basicBindingOrder(){
        return BindingBuilder.bind(queueOrder())
                .to(basicExchange())
                .with(env.getProperty("mq.pay.routing.orderkey"));
    }
    /**
     * 将秒杀队列绑定到交换机上
     * @return
     */
    @Bean
    public Binding basicBindingSeckillOrder(){
        return BindingBuilder.bind(queueSeckillOrder())

                .to(basicExchange())
                .with(env.getProperty("mq.pay.routing.seckillorderkey"));
    }




}
