package entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @author yanming
 * @version 1.0 2020/12/30
 */
@Component
public class MyFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        try {
            //使用RequestContextHolder工具获取request相关变量
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes!=null){
                //取出Request
                HttpServletRequest request = attributes.getRequest();
                //获取头文件信息的key
                Enumeration<String> headerNames = request.getHeaderNames();
                if(headerNames != null){
                    //获取头文件的key
                    while(headerNames.hasMoreElements()){
                        String name = headerNames.nextElement();
                        //获取头文件单位values
                        String values = request.getHeader(name);
                        //将令牌数据添加到头文件中
                        template.header(name,values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
