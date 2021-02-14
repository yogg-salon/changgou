package com.changgou.seckill.service.impl;

import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.task.MultiThreadingCreateOrder;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import entity.StatusCode;
import entity.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/****
 * @Author:admin
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;
    @Autowired
    private IdWorker idWorker;

    /**
     * 关闭订单回滚库存
     * @param username
     */
    @Override
    public void closeOrder(String username) {
        //获取seckillStatus从redis中
        SeckillStatus seckillStatus = (SeckillStatus)redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_STATUS).get(username);
        //获取Redis订单信息
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).get(username);
        //如果Redis中有订单信息，说明用户未支付
        if(seckillStatus!=null && seckillOrder !=null){
            //删除订单
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).delete(username);
            //回滚库存
            //1、从Redis中获取该上平
            SeckillGoods seckillGoods  = (SeckillGoods)redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
        //如果redis中没有，从数据库中加载
            if(seckillGoods!=null){
               seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            }
            // 数量+1
            Long increment = redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_COUNT).increment(seckillStatus.getGoodsId(), 1);
            seckillGoods.setStockCount(increment.intValue());
            redisTemplate.boundListOps(SystemConstants.SEC_KILL_GOODS_COUNT_LIST+seckillStatus.getGoodsId()).leftPush(seckillStatus.getGoodsId());
            // 数据同步到Redis中
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+seckillStatus.getTime()).put(seckillStatus.getGoodsId(),seckillGoods);
            //清理排队标示
            redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_COUNT).delete(seckillStatus.getUsername());
            redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_STATUS).delete(seckillStatus.getUsername());

        }


    }

    /**
     * 更新订单状态
     * @param out_trade_no
     * @param transaction_id
     * @param username
     */
    @Override
    public void updatePayStatus(String out_trade_no, String transaction_id, String username) {
        SeckillOrder seckillOrder =(SeckillOrder) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).get(username);
    //从数据库中查询秒杀订单
        //修改状态
        seckillOrder.setStatus("1");
        //支付支付时间
        seckillOrder.setPayTime(new Date());
        //同步到MySQL中
        seckillOrderMapper.insertSelective(seckillOrder);
        //清空redis缓存
        redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).delete(username);
        //清空用户排队数据
        redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_COUNT).delete(username);
        //删除抢购信息
        redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_STATUS).delete(username);


    }

    /**
     * 添加秒杀订单
     * @param id
     * @param time
     * @param username
     * @return
     */
    @Override
    public Boolean add(Long id, String time, String username) {
        Long userQueueCount = redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_COUNT).increment(username,1);
       if(userQueueCount>1){
       //如果大于1则表明有重复下单
           throw new  RuntimeException(String.valueOf(StatusCode.REPERROR));
       }
        SeckillStatus seckillStatus = new SeckillStatus(username,new Date(),1,id,time);
        //将秒杀抢单信息存入到redis中，这里采用list方式存储，左存右取是队列 同方向存取是栈
        redisTemplate.boundListOps(SystemConstants.SEC_KILL_ORDER_QUEUE).leftPush(seckillStatus);

        //将抢单状态存入redis中
        redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_STATUS).put(username,seckillStatus);

        multiThreadingCreateOrder.createOrder();
        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }

    /**
     * 查询抢购状态
     * @param username
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus)redisTemplate.boundHashOps(SystemConstants.USER_QUEUE_STATUS).get(username);
    }
}
