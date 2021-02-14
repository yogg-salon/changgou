package com.changgou.seckill.task;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.IdWorker;
import entity.SystemConstants;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author yanming
 * @version 1.0 2021/1/18
 */
@Component
public class MultiThreadingCreateOrder {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 多线程下单操作
     *      *
     *
     */
    @Async
    public  void createOrder(){

       //从订单秒杀队列中获取排队信息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps(SystemConstants.SEC_KILL_ORDER_QUEUE);

        try{
            Object good = redisTemplate.boundListOps(SystemConstants.SEC_KILL_GOODS_COUNT_LIST + seckillStatus.getGoodsId()).rightPop();
            if(good==null){
                //清理当前用户的排队信息
                clearQueue(seckillStatus);
                return;
            }
            if(seckillStatus!=null){
                String time=seckillStatus.getTime();
                String username=seckillStatus.getUsername();
                Long id =seckillStatus.getGoodsId();

                //获取商品数据
                SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + time).get(id);

                //如果没有库存，则直接抛出异常
                if(goods==null || goods.getStockCount()<=0){
                    throw new RuntimeException("已售罄!");
                }
                //如果有库存，则创建秒杀商品订单
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setId(idWorker.nextId());
                seckillOrder.setSeckillId(id);
                seckillOrder.setMoney(goods.getCostPrice());
                seckillOrder.setUserId(username);
                seckillOrder.setCreateTime(new Date());
                seckillOrder.setStatus("0");

                //将秒杀订单存入到Redis中
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).put(username,seckillOrder);
                //redia中商品库存减少
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_COUNT).increment(id,-1);//商品数量递减
                //库存减少
                goods.setStockCount(goods.getStockCount()-1);

                //判断当前商品是否还有库存
                if(goods.getStockCount()<=0){
                    //并且将商品数据同步到MySQL中
                    seckillGoodsMapper.updateByPrimaryKeySelective(goods);
                    //如果没有库存,则清空Redis缓存中该商品
                    redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + time).delete(id);
                }else{
                    //如果有库存，则直数据重置到Reids中
                    redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + time).put(id,goods);
                }
                seckillStatus.setStatus(2);//抢单成功
                seckillStatus.setOrderId(seckillOrder.getId());
                seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney()));
                redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_STATUS).put(username,seckillStatus);
                sendTimerMessage(seckillStatus);

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 清理用户排队信息
     * @param seckillStatus
     */
    private void clearQueue(SeckillStatus seckillStatus) {
        //清理用户排队信息
        redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_COUNT).delete(seckillStatus.getUsername());
        //清理抢单状态
        redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_STATUS).delete(seckillStatus.getUsername());


    }
    @Autowired
    public Environment env;

    /**
     * 发送延时消息到rabbitMQ
     * @param seckillStatus
     */
    public void sendTimerMessage(SeckillStatus seckillStatus){
        rabbitTemplate.convertAndSend(env.getProperty("mq.pay.queue.seckillordertimerdelay"), (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
               message.getMessageProperties().setExpiration("10000");//设置过期时间
                return null;
            }
        });
    }

}
