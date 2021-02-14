package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author yanming
 * @version 1.0 2020/12/26
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";
    //用户登录地址
    public static final String  USER_LOGIN_URL="http://localhost:9001/oauth/login";
    /**
     * 全局过滤器
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取Request 、Response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //获取请求的URI
        String path =request.getURI().getPath();
        //如果是登录、goods等开放的微服务,则直接放行
        if(path.startsWith("/api/user/login") || path.startsWith("/api/brand/search/")|| URLFilter.hasAuthorize(path)){
            //放行
            Mono<Void> filter = chain.filter(exchange);
            return filter;
        }
        //获取头文件中的令牌信息
        String token =request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        //如果头文件中没有，则从请求参数中获取
        if(StringUtils.isEmpty(token)){
            token=request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }
        //从cookie中获取令牌数据
        HttpCookie first =request.getCookies().getFirst(AUTHORIZE_TOKEN);
        if(first!=null){
            token=first.getValue();
        }

        if(StringUtils.isEmpty(token)){
           // response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            //return  response.setComplete();
            return needAuthorization(USER_LOGIN_URL+"?FROM="+request.getURI(),exchange);
        }
        //解析令牌数据
        try {
            //Claims claims = JwtUtil.parseJWT(token);
            request.mutate().header(AUTHORIZE_TOKEN,"Bearer "+token);
        }catch (Exception e){
            e.printStackTrace();
            //解析失败，响应401错误
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //放行
        return chain.filter(exchange);



    }

    /**
     * 响应设置
     * @param userLoginUrl
     * @param exchange
     * @return
     */
    private Mono<Void> needAuthorization(String userLoginUrl, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        //获取response对象
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("Location",userLoginUrl);
        return exchange.getResponse().setComplete();//发送一个请求；
    }

    /**
     * 过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
