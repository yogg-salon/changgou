package com.changgou.order.controller;

import entity.TokenDecode;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yanming
 * @version 1.0 2020/12/30
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/cart")
public class CartController {
    @Autowired
    private TokenDecode tokenDecode;

    @Autowired
    private CartService cartService;
    /**
     * 加入购物车
     * @param num :购买的数量
     * @Param id ：购买的商品的Id
     * @return
     */
    @RequestMapping(value = "/add")
    public Result add(Integer num, Long id){
        //用户名
        String username =tokenDecode.getUserInfo().get("username");
        //将商品加入购物车
        cartService.add(num,id,username);
        return  new Result(true, StatusCode.OK ,"加入购物车成功!");
    }



    @GetMapping(value = "/list")
    public Result<List<OrderItem>> list(){
        //用户名
//        String username ="szitheima";
        String username =tokenDecode.getUserInfo().get("username");
        List<OrderItem> orderItems =cartService.list(username);
        return new Result<List<OrderItem>>(true,StatusCode.OK ,"购物车列表查询成功!",orderItems);
    }



}
