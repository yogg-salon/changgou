package com.changgou.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * @author yanming
 * @version 1.0 2021/1/19
 */
@Configuration
public class QueueConfig {

    /**
     * 短信发送队列
     *
     */
    public static final String  QUEUE_MESSAGE="queue.message";
    /**
     * 交换机
     */
    public static final String  DLX_EXCHANGE ="dlx.exchange";


    /**
     * 信息发送队列 延迟缓冲
     */
    public static final String QUEUE_MESSAGE_DELAY="queue.message.delay";
    /**
     * 短信发送队列
     * @return
     */
    @Bean
    public Queue messageQueue(){
        return new Queue(QUEUE_MESSAGE,true);
    }
    /**
     * 延时队列
     */
    @Bean
    public Queue delayMessageQueue(){
        return QueueBuilder.durable(QUEUE_MESSAGE_DELAY)
                .withArgument("x-dead-letter-exchange",DLX_EXCHANGE) //超时消息进入死信队列，绑定死信交换机
                .withArgument("x-dead-letter-routing-key",QUEUE_MESSAGE)//绑定指定的routing-key
                .build();
    }
    /**
     * 创建交换机
     * @return
     */
    @Bean
    public DirectExchange directExchange(){
        return  new DirectExchange(DLX_EXCHANGE);
    }

    /**
     * 交换机与队列绑定
     * @param messageQueue
     * @param directExchange
     * @return
     */


    @Bean
    public Binding basicBinding(Queue messageQueue,DirectExchange directExchange){
        return BindingBuilder
                .bind(messageQueue)
                .to(directExchange)
                .with(QUEUE_MESSAGE);
    }





}
