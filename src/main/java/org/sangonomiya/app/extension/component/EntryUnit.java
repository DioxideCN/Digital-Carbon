package org.sangonomiya.app.extension.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sangonomiya.app.core.Response;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 账号未登录拒绝策略
 * @author Dioxide.CN
 * @date 2023/3/1 19:31
 * @since 1.0
 */
@Component
public class EntryUnit implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(new ObjectMapper().writeValueAsString(Response.fail("未登录账号")));
        out.flush();
        out.close();
    }

}
