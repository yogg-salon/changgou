package com.yan.changgou.search.controller;

import com.changgou.pojo.SkuInfo;
import com.changgou.search.feign.SkuFeign;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author yanming
 * @version 1.0 2020/12/24
 */
@Controller
@RequestMapping(value="/search")
public class SkuController {
    @Autowired
    private SkuFeign skuFeign;
//    @GetMapping(value = "/list")
    /**
     * 实现搜索调用
     */
   /* public String search(@RequestParam(required = false) Map searchMap , Model model){
        //调用changgou-service-search 微服务
        Map resultMap =skuFeign.search(searchMap);
        //s搜索数据结果
        model.addAttribute("result",resultMap);
        //搜索条件
        model.addAttribute("searchMap",searchMap);
        //请求地址
        String[] urls =url(searchMap);
        model.addAttribute("url",urls[0]);
        model.addAttribute("sorturl",urls[1]);
        //分页计算
        Page<SkuInfo> page =new Page<SkuInfo>(
            Long.parseLong(resultMap.get("totalPages").toString()),//总计录数
            Integer.parseInt(resultMap.get("pageNum").toString()),
            Integer.parseInt(resultMap.get("pageSize").toString())
        );
        model.addAttribute("page",page);
        return "search";
    }

    *//**
     * url组装和处理
     * @param searchMap
     * @return String[]
     *//*

    private String[] url(Map<String,String> searchMap) {
        String url="/search/list";
        String sorturl="/search/list";
        if(searchMap!=null&&searchMap.size()>0){
            url+="?";
            sorturl+="?";
            for(Map.Entry<String,String> entry : searchMap.entrySet()){

                String key=entry.getKey();
                //如果是排序则跳过拼接的地址，因为有数据,
                // 每次排序会恢复第一页排序，不需要之前的排序信息
                if(key.equals("sortField")||key.equals("sortRule")){
                    //排序信息部拼装
                    continue;
                }
                //分页跳过
                if(key.equalsIgnoreCase("pageNum")){
                    continue;
                    //分页参数不拼接
                }
                url+=key+"="+entry.getValue()+"&";
                sorturl+=key+"="+entry.getValue()+"&";
            }
            //去掉最后一个&
            url=url.substring(0,url.length()-1);
            url=url.substring(0,sorturl.length()-1);
        }
        return new String[]{url,sorturl};
    }*/

    @GetMapping("/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {
        //1.调用搜索微服务的 feign  根据搜索的条件参数 查询 数据
        Map resultmap = skuFeign.search(searchMap);
        //2.将数据设置到model中     (模板文件中 根据th:标签数据展示)
        //搜索的结果设置
        model.addAttribute("result", resultmap);

        //3.设置搜索的条件 回显
        model.addAttribute("searchMap",searchMap);

        //4.记住之前的URL
        //拼接url
        String url = url(searchMap);
        model.addAttribute("url",url);
        //创建一个分页的对象  可以获取当前页 和总个记录数和显示的页码(以当前页为中心的5个页码)
        Page<SkuInfo> infoPage = new Page<SkuInfo>(
                Long.valueOf(resultmap.get("total").toString()),
                Integer.valueOf(resultmap.get("pageNum").toString()),
                Integer.valueOf(resultmap.get("pageSize").toString())
        );

        model.addAttribute("page",infoPage);
        //3.返回
        return "search";
    }

    private String url(Map<String, String> searchMap) {
        String url = "/search/list";
        if(searchMap!=null && searchMap.size()>0){
            url+="?";
            for (Map.Entry<String, String> stringStringEntry : searchMap.entrySet()) {
                String key = stringStringEntry.getKey();// keywords / brand  / category
                String value = stringStringEntry.getValue();//华为  / 华为  / 笔记本
                if(key.equals("pageNum")){
                    continue;
                }
                url+=key+"="+value+"&";
            }

            //去掉多余的&
            if(url.lastIndexOf("&")!=-1){
                url =  url.substring(0,url.lastIndexOf("&"));
            }

        }
        return url;
    }


}
