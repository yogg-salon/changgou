package com.changgou.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;


/**
 * @author yanming
 * @version 1.0 2020/12/30
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true,securedEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    //公钥

    private static final String PUBLIC_KEY="public.key";

    /**
     * 定义JwtTokenStore
     * @param jwtAccessTokenConverter
     * @return
     */
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter){
        return  new JwtTokenStore(jwtAccessTokenConverter);
    }

    /**
     * 定义JwtAccessTokenConvernter
     * @return
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter converter =new JwtAccessTokenConverter();
        converter.setVerifierKey(getPubkey());
        return converter;
    }

    /**
     * 获取非对成加密公钥 key
     * @return
     */
    private String getPubkey() {
        Resource resource =new ClassPathResource(PUBLIC_KEY);
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br =new BufferedReader(inputStreamReader);
            return br.lines().collect(Collectors.joining("\n"));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    /**
     * http 安全配置，对每个到达系统的http请求连接进行校验
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        //所有的请求必须认证通过
        http.authorizeRequests().antMatchers("/user/add")
                .permitAll().anyRequest().authenticated();//配置地址放行其他地址需要认证授权


//        super.configure(http);http
    }
}
