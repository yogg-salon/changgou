package com.changgou.service;

import java.util.Map;

/**
 * @author yanming
 * @version 1.0 2021/1/15
 */
public interface WeixinPayService {
    /**
     * 半小时未支付,先关闭微信支付，在关闭支付订单
     * @param orderId
     * @return
     */
    Map<String,String> closePay(Long orderId);

    /**
     * 创建二维码
     * @param parameter  其他附加信息 ，包括订单号，金额等
     *
     * @return
     */
    public Map   createNative(Map<String,String> parameter);


    /**
     * 查询订单状态
     * @param out_trade_no：客户端自定义订单编号
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

}
