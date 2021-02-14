package com.changgou.goods.feign;

import com.changgou.goods.pojo.Category;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yanming
 * @version 1.0 2020/12/25
 */
@FeignClient(value = "goods")
@RequestMapping("/category")
public interface CategoryFeign {
    /**
     * 获取分类对象信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Category> findById(@PathVariable(name="id") Integer id);
}
