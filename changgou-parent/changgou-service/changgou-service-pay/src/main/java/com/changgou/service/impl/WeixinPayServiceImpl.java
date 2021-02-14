package com.changgou.service.impl;

import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yanming
 * @version 1.0 2021/1/15
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${weixin.appid}")
    private String appid;   //微信公众号的唯一标识

    @Value("${weixin.partner}")
    private String partner;   // 微信财付通的商户账号

    @Value("${weixin.partnerkey}")
    private String partnerkey;// 财付通平台的商户密钥

    @Value("${weixin.notifyurl}")
    private String notifyurl;//回调地址

    /**
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> closePay(Long orderId) {
        try {
            //参数设置
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("appid",appid);//应用ID
            paramMap.put("mch_id",partner);//商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());//随机字符串
            paramMap.put("out_trade_no",String.valueOf(orderId));//商家的唯一订单号
            //将Map数据转成XMl字符
            String xmlParam = WXPayUtil.generateSignedXml(paramMap,partnerkey);
            //确定url
            String url="https://api.mch.weixin.qq.com/pay/closeorder";
            //发送请求
            HttpClient httpClient = new HttpClient(url);
            //https
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();//
            String content = httpClient.getContent();
            //将返回数据解析成Map
            return  WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建二维码
     * @param parameter  相关参数
     *
     * @return
     */

    @Override
    public Map createNative( Map<String,String> parameter) {
        Map param =new HashMap();
        param.put("appid",appid);//应用ID
        param.put("mch_id",partner);//商户ID号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body","畅购");//订单描述
        param.put("out_trade_no",parameter.get("out_trade_no"));
        param.put("total_fee",parameter.get("total_fee"));//交易金额
        param.put("spbill_create_ip","127.0.0.1");//终端IP
        param.put("notify_url",notifyurl);//回调地址
        param.put("trade_type","NATIVE");//交易类型
        //将参数转成xml字符，携带签名

        try {
            String paramxml = WXPayUtil.generateSignedXml(param,partnerkey);

            //通过httpclient发送请求
            HttpClient httpClient =new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramxml);
            httpClient.post();//必须以post发送
    //        获取返回结果
            String content = httpClient.getContent();
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);
            System.out.println("stringMap"+stringMap);
            //获取部分页面所需参数

            Map<String,String> dataMap = new HashMap<String,String>();
            dataMap.put("code_url",stringMap.get("code_url"));
            dataMap.put("out_trad_no",parameter.get("out_trade_no"));
            dataMap.put("total_fee",parameter.get("total_fee"));
            return dataMap;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 查询订单状态
     * @param out_trade_no：客户端自定义订单编号
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            Map param = new HashMap();
            param.put("appid",appid);                            //应用ID
            param.put("mch_id",partner);                         //商户号
            param.put("out_trade_no",out_trade_no);              //商户订单编号
            param.put("nonce_str",WXPayUtil.generateNonceStr()); //随机字符
            //2、将参数转成xml字符，并携带签名
            String paramXml = WXPayUtil.generateSignedXml(param,partnerkey);

            //3、发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //4、获取返回值，并将返回值转成Map
            String content = httpClient.getContent();
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
