import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.Date;

/**
 * @author yanming
 * @version 1.0 2020/12/26
 */
public class JwtTest {


    /**
     * 创建Jwt令牌
     */
    @Test
    public void testCreateJwt(){
        JwtBuilder builder = Jwts.builder();
                builder. setId("888")// 唯一编号
                .setSubject("小白")//设置主题
                .setIssuedAt(new Date()) //设置签发日期
                .setExpiration(new Date(System.currentTimeMillis()+20000))
                .signWith(SignatureAlgorithm.HS256,"yan".getBytes());//设置签名 使用 ES256算法
        System.out.println(builder.compact());

    }
    @Test
    public void parseJwt(){
        String str = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE2MDg5ODgzMzZ9.Gd3z9zaYgrkA9fis4wQDoCnsLqeFlJ34jL6Su_55wu8";
        Jws<Claims> yan =Jwts.parser().setSigningKey("yan").parseClaimsJws(str);
        System.out.println(yan.getBody());
    }


}
