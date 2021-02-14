package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yanming
 * @version 1.0 2020/12/30
 */
@FeignClient(name="user")
@RequestMapping("/user")

public interface UserFeign {
    /**
     * 通过id查出username
     * @param id
     * @return
     */
    @GetMapping("/load/{id}")

    public Result<User> findByUsername(@PathVariable(name="id") String id);

    /**
     * 添加用户积分
     * @param points
     * @return
     */
    @GetMapping(value="/points/add")
    public Result addPoints(@RequestParam(value = "points") Integer points);




}
