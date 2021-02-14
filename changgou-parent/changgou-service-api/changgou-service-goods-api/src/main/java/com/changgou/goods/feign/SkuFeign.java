package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.goods.feign *
 * @since 1.0
 */
@FeignClient(value="goods")
@RequestMapping("/sku")
public interface SkuFeign {
    /**
     * 递减库存
     * @param username
     * @return
     */
    @PostMapping(value = "decr/count")
    Result decrCount(@RequestParam(value = "username") String username);
    /**
     * 查询符合条件的状态的SKU的列表
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
    public Result<List<Sku>> findByStatus(@PathVariable(name="status") String status);
    /**
     * 条件搜索
     * @param sku
     * @return
     */
    @PostMapping(value = "/search")
    public Result<List<Sku>> findList(@RequestBody(required = false)Sku sku);

    /**
     * 根据ID查询SKU的信息
     * @param id sku的id
     * @return
     */
    @GetMapping(value="/{id}")
    public Result<Sku> findById(@PathVariable(value = "id",required = true)Long id);

}
