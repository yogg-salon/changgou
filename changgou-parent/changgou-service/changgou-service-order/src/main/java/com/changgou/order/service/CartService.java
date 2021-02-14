package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

/**
 * @author yanming
 * @version 1.0 2020/12/30
 */
public interface CartService {

    /**
     * 添加购物车
     * @param num   购买商品数量
     * @param id   购买ID
     * @param username  购买用户
     */
    void add(Integer num,Long id,String username);

    /**
     * 查询用户的购物车数据
     * @param username
     * @return
     */
    List<OrderItem> list(String username);


}
