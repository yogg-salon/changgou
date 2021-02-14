package com.changgou.seckill.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yanming
 * @version 1.0 2021/1/19
 */

@FeignClient(name="pay")
@RequestMapping("/weixin/pay")
public interface WeixinPayFeign {
    @RequestMapping("/close/pay")
    Result closePay(Long orderId);
}
