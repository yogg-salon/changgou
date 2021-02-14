package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author yanming
 * @version 1.0 2021/1/19
 */
@Component
@RabbitListener(queues = " {mq.pay.queue.seckillorder}")
public class SeckillOrderPayMessageListener {
    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitHandler
    public void consumeMessage(@Payload String message){
        System.out.println(message);
        Map<String,String> resultMap = JSON.parseObject(message,Map.class);
        System.out.println("监听到的信息"+resultMap);
        String return_code = resultMap.get("return_code");
        String result_code = resultMap.get("result_code");
        if(return_code.equalsIgnoreCase("success")){
            //获取订单单号
            String out_trade_no =resultMap.get("out_trade_no");
            //获取附加信息
            Map<String,String> attachMap = JSON.parseObject(resultMap.get("attach"), Map.class);
            //订单支付成功

            if(result_code.equalsIgnoreCase("success")){
                //修改订单状态
                seckillOrderService.updatePayStatus(out_trade_no,resultMap.get("transaction_id"),attachMap.get("username"));
            }else{
                //支付失败，删除订单，回滚库存
                seckillOrderService.closeOrder(attachMap.get("username"));
            }



        }
    }
}
