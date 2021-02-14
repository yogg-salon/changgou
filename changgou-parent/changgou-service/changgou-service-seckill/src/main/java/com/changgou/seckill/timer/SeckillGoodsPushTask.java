package com.changgou.seckill.timer;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import entity.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author yanming
 * @version 1.0 2021/1/18
 */

/**
 * 时间组件 定时任务
 */
@Component
public class SeckillGoodsPushTask {
//    @Scheduled(cron = "0/1 * * * * ?")
//    public void loadGoodsPushRedis(){
//        System.out.println("task demo");
//    }
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
//    @Scheduled(cron = "0/30 * * * * ?")
    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis(){

        //获取时间集合
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date date : dateMenus) {
            // 获得namespace 的一部分 SewckillGoods_21118
            String extName =DateUtil.date2Str(date,DateUtil.PATTERN_YYYYMMDDHH);
            //根据时间段数据查询对应的秒杀商品数据
            Example example =new Example(SeckillGoods.class);
            Example.Criteria criteria =example.createCriteria();
            criteria.andEqualTo("status",1);//已经通过商品审核的秒杀商品
            criteria.andGreaterThan("stockCount",0);//库存大于0的商品
            criteria.andGreaterThanOrEqualTo("startTime",date);
            //开始时间大于等于活动开始时间的商品
            criteria.andLessThan("endTime",DateUtil.addDateHours(date,2));
            //活动结束时间小于开始时间的+2小时的
            Set keys = redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+ extName).keys();
            if(keys!=null&&keys.size()>0){
                criteria.andNotIn("id",keys);
            }
            //查询数据,将秒杀数据存入到redis缓存中
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);
            for (SeckillGoods seckillGood : seckillGoods) {
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+extName).put(seckillGood.getId(),seckillGood);
               //商品数据队列存储，防止高并发超卖
                Long[] longs = pushIds(seckillGood.getStockCount(), seckillGood.getId());
                redisTemplate.boundListOps(SystemConstants.SEC_KILL_GOODS_COUNT_LIST+seckillGood.getId()).leftPushAll(longs);
                //自增计数器
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_COUNT).increment(seckillGood.getId(),seckillGood.getStockCount());

                redisTemplate.expireAt(SystemConstants.SEC_KILL_GOODS_PREFIX+extName,DateUtil.addDateHours(date,2));
            }

        }
    }
    public Long[] pushIds(int len,Long id){
        Long[] ids= new Long[len];
        for (int i = 0; i < ids.length; i++) {
            ids[i]=id;
        }
        return ids;
    }
}
