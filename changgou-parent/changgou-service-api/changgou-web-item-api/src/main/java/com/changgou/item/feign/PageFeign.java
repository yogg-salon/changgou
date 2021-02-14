package com.changgou.item.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yanming
 * @version 1.0 2020/12/25
 */
@FeignClient(name="item")
@RequestMapping("/page")
public interface PageFeign {
    /**
     * 根据SpuId生成静态页
     * @param id
     * @return
     */


    @RequestMapping("/createHtml/{id}")
    Result createHtml(@PathVariable(name="id")Long id);
}
