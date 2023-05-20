package org.sangonomiya.app.extension.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dioxide.CN
 * @date 2023/2/28 22:11
 * @since 1.0
 */
@Component
@Slf4j
@SuppressWarnings("all")
public class JwtUnit {

    private static final String CLAIM_KEY_USERNAME = "sub";
    private  static  final String CLAIM_KEY_CREATED = "created";

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 根据用户信息生成token
     * @param userDetails 用户信息
     * @return 返回生成的token信息
     */
    public String generateToken(UserDetails userDetails){
        Map<String,Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }


    /**
     * 从token中获取登录用户名
     * @param token token信息
     * @return 返回token所属的用户的用户名
     */
    public String getUserNameFromToken(String token){
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        }catch (Exception e){
            username = null;
        }
        return username;
    }

    /**
     * 验证token是否有效
     * @param token token信息
     * @param userDetails 用户信息
     * @return true 有效 false 无效
     */
    public boolean validateToken(String token, UserDetails userDetails){
        assert userDetails != null;
        String username = getUserNameFromToken(token);
        return username.equals(userDetails.getUsername()) && isTokenExpired(token);
    }

    /**
     * 判断token是否可以被刷新
     * @param token token信息
     * @return 返回该token是否支持刷新
     */
    public boolean canRefresh(String token){
        return isTokenExpired(token);
    }

    /**
     * 刷新token
     * @param token token信息
     * @return 将刷新后的token进行返回
     */
    public String refreshToken(String token){
        Claims claims = getClaimsFromToken(token);
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    /**
     * 判断token是否过期
     * @param token token信息
     * @return true 未过期 false 过期
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return !expiredDate.before(new Date());
    }

    /**
     * 从token中获取失效时间
     * @param token token信息
     * @return 返回该token的失效时间
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 从token中获取荷载
     * @param token token信息
     * @return 返回该token的荷载
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }


    /**
     * 根据荷载生成JWT TOKEN
     * @param claims 荷载
     * @return 返回token
     */
    private String generateToken(Map<String,Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成token的失效时间
     * @return 返回token失效时间戳
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

}
