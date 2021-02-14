package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.oauth.controller *
 * @since 1.0
 */
@Controller
@RequestMapping("/oauth")
public class LoginRedirectController {
    /**
     * 跳转到登录页面,并且记录访问原页，从页面的vue中获取访问原页信息
     * @param From
     * @param model
     * @return
     */
    @RequestMapping("/login")
    public String login(@RequestParam(value = "FROM",defaultValue = "",required = false) String From, Model model) {
        model.addAttribute("from",From);
        return "login";
    }


}
