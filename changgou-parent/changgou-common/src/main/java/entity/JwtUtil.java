package entity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * @author yanming
 * @version 1.0 2020/12/26
 */
public class JwtUtil {

    //有效期
    public static final Long JWT_TTL = 3600000L;
    //Jwt令牌信息
    public static final String JWT_KEY="yan";


    //创造JWT令牌
    public static String createJWT(String id ,String subject,Long ttlMillis) {
        //指定JWT算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        //获取当前系统时间
        Long nowMills = System.currentTimeMillis();
        Date now = new Date(nowMills);
        //如果令牌有效期为null，则默认设置有效期1小时
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }
        //令牌过期时间设置
        long expMillis = nowMills + ttlMillis;
        Date expDate = new Date(expMillis);
        //生成秘钥
        SecretKey secretKey = generalKey();
        //封装Jwt令牌信息
        JwtBuilder builder = Jwts.builder()
                .setId(id)                    //唯一的ID
                .setSubject(subject)          // 主题  可以是JSON数据
                .setIssuer("admin")          // 签发者
                .setIssuedAt(now)             // 签发时间
                .signWith(signatureAlgorithm, secretKey) // 签名算法以及密匙
                .setExpiration(expDate);      // 设置过期时间
        return builder.compact();
    }

    /**
     * 加密生成secrekKey
     * @return
     */
    private static SecretKey generalKey() {
        byte[] encodedKey = Base64.getEncoder().encode(JWT_KEY.getBytes());
        SecretKey key =new SecretKeySpec(encodedKey,0, encodedKey.length, "AES");
        return key;
    }
    public static Claims parseJWT(String jwt) throws Exception{
        SecretKey secretKey =generalKey();
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt).getBody();
    }


}
