package com.changgou.goods.feign;

import com.changgou.goods.pojo.Spu;
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
@RequestMapping("/spu")
public interface SpuFeign {
    /**
     * 根据SpuID查询信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable(name="id") Long id);

    }
