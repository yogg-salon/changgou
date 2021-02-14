package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yanming
 * @version 1.0 2021/1/15
 */
@RestController
@RequestMapping(value = "/weixin/pay")
@CrossOrigin
public class WeixinPayController {
    @Autowired
    private WeixinPayService weixinPayService;
    @Value("${mq.pay.exchange.order}")
    private String exchange;
    @Value("${mq.pay.queue.order}")
    private String queue;
    @Value("${mq.pay.routing.key}")
    private String routing;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建二维码
     * @param parameter
     * @return
     */
    @RequestMapping("/create/native")
    public Result createNative(@RequestParam Map<String,String> parameter){
        Map resultMap = weixinPayService.createNative(parameter);
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功");
    }

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String out_trade_no){
        Map<String,String> resultMap = weixinPayService.queryPayStatus(out_trade_no);
        return new Result(true,StatusCode.OK,"查询状态成功！",resultMap);
    }

    /**
     * 支付回调
     * @param request
     * @return
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request){

        try(InputStream inputStream=request.getInputStream();ByteArrayOutputStream outStream = new ByteArrayOutputStream()){
            //读取支付回调数据

            byte[] buffer =new byte[1024];
            int len =0;
            while((len=inputStream.read(buffer))!=-1){
                outStream.write(buffer,0,len);
            }
            //将支付回调数据转换成xml字符串
            String result = new String(outStream.toByteArray(), "utf-8");
            //将xml字符串转换成Map结构
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            Map<String,String> parameters =JSON.parseObject("attach",Map.class);
            System.out.println(parameters);
            rabbitTemplate.convertAndSend(parameters.get("exchange"),parameters.get("routingkey"),JSON.toJSONString(map));

            //将消息发送到mq
//            rabbitTemplate.convertAndSend(exchange,routing, JSON.toJSONString(map));
            //响应数据设置
            Map respMap = new HashMap();
            respMap.put("return_code","SUCCESS");
            respMap.put("return_msg","OK");
            return WXPayUtil.mapToXml(respMap);
        }catch (Exception e){
            e.printStackTrace();
            //打印错误日志
        }
        return null;
    }

    /**
     * 一个测试路径
     * @return
     */
    @RequestMapping("/test")
    public String test(){
        //动态的从attach参数中获取数据
        Map<String,String> attach = new HashMap<>();
        attach.put("username","zhangsan");
        attach.put("queue","queue.seckillorder");//队列名称
        attach.put("routingkey","queue.seckillorder");//路由key
        attach.put("exchange","exchange.seckillorder");
        // {routingkey=queue.seckillorder, exchange=exchange.seckillorder, queue=queue.seckillorder, username=szitheima}
        rabbitTemplate.convertAndSend(attach.get("exchange"),attach.get("routingkey"),"数据");

        return null;
    }
    @RequestMapping("/close/pay")
    public Result closePay(Long orderId){
     return  new Result(true,StatusCode.OK,"关闭成功",weixinPayService.closePay(orderId));
    }
}
