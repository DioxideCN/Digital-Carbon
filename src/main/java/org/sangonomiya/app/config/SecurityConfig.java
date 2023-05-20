package org.sangonomiya.app.config;

import org.sangonomiya.app.extension.component.EntryUnit;
import org.sangonomiya.app.extension.component.AccessUnit;
import org.sangonomiya.app.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

/**
 * SpringSecurity策略
 * @author Dioxide.CN
 * @date 2023/2/28 14:55
 * @since 1.0
 */
@Configuration
@SuppressWarnings("all")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    IUserService userAuthService;

    @Autowired
    private AccessUnit accessUnit;
    @Autowired
    private EntryUnit entryUnit;

    /**
     * 接口请求拦截器，所有未被WebSecurity忽略的接口都会先走这个方法进行用户认证
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }

    /**
     * 白名单过滤器，所有符合下列表达式的接口url都不会被security本身拦截
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(
                        "/api/user/auth/**",
                        "/css/**",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/api/payment/callback",
                        "/websocket/**"
                );
    }

    /**
     * 过滤器，所有除了白名单过滤器内的url都会走这里进行处理，包括鉴权和防火墙
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception{
        //使用JWT,不需要csrf
        http.csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 允许登录访问的接口
                .antMatchers(
                        "/api/user/auth/**",
                        "/doc.html",
                        "/api/payment/callback",
                        "/websocket/**"
                ).permitAll()
                // 除了上面，所有的请求都需要认证
                .anyRequest()
                .authenticated()
                .and()
                .headers()
                .cacheControl();

        // 添加jwt登录授权过滤器
        http.addFilterBefore(JwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        // 添加自定义未授权和未登录的结果返回
        http.exceptionHandling()
                .accessDeniedHandler(accessUnit) // 权限不足
                .authenticationEntryPoint(entryUnit); // 未登录账号
    }

    /**
     * 返回一个Service方法，是通过username调用UserAuthService中的获取用户方法获取一个UserVO(UserDetails)
     * @return UserAuthService.getUserByUsername()方法的函数式封装
     */
    @Override
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userAuthService.getUserByUsername(username);
    }

    /**
     * 定义全局密码加密规则
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationConfig JwtAuthenticationTokenFilter() {
        return new JwtAuthenticationConfig();
    }

}
