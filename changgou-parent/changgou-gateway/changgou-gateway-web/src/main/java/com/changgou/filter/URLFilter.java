package com.changgou.filter;

import org.springframework.stereotype.Component;

/**
 * @author yanming
 * @version 1.0 2020/12/30
 */
@Component
public class URLFilter {
    /**
     * 要放行的路径
     */
    public static final String  noAuthorizeurls = "/api/user/add,/api/user/login";



    public static boolean hasAuthorize(String url) {
        String[] split = noAuthorizeurls.split(",");
        for (String s:split) {
            if(s.equals(url)){
                return  true;
            }
        }
        return  false;
    }
}
