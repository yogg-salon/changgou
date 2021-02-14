package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yanming
 * @version 1.0 2020/12/30
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    /**
     * 商品加入购物车
     * @param num   购买商品数量
     * @param id   购买ID
     * @param username  购买用户
     */
    @Override
    public void add(Integer num, Long id, String username) {
        if(num<=0){
//删除该商品的购物车数据
            redisTemplate.boundHashOps("Cart_"+username).delete(id);
            //如果传入的数量值<=0,则删除该商品的购物车数据
            return;
        }


        //查询SKu
        Result<Sku> resultSku = skuFeign.findById(id);
        if(resultSku!=null && resultSku.isFlag()){
            //获取SKU
            Sku sku =resultSku.getData();
            //获取spu
            Result<Spu> resultSpu= spuFeign.findById(sku.getSpuId());
            //将SKU转换成OrderItem
            OrderItem orderItem = sku2OrderItem(sku,resultSpu.getData(),num);
            /**
             * 购物车存入到Redis
             * namespace =Cart_[username]
             * key=id(sku)
             * value=OrderItem
             */
            redisTemplate.boundHashOps("Cart_"+username).put(id,orderItem);
        }
    }

    /**
     * 查询购物车数据
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        //查询购物车数据
        List<OrderItem> orderItems =redisTemplate.boundHashOps("Cart_"+username).values();
        return orderItems;
    }

    /**
     * 将sku转换成OrderItem
     * @param sku
     * @param data
     * @param num
     * @return
     */
    private OrderItem sku2OrderItem(Sku sku, Spu data, Integer num) {
        OrderItem orderItem =new OrderItem();
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num*orderItem.getPrice());//数量*单价
        orderItem.setPayMoney(num*orderItem.getPrice());
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight()); //重量= 单个重量* 数量
        //分类Id
        orderItem.setCategoryId1(data.getCategory1Id());
        orderItem.setCategoryId2(data.getCategory2Id());
        orderItem.setCategoryId3(data.getCategory3Id());
        return  orderItem;
    }
}
