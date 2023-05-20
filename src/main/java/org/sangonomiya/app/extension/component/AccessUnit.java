package org.sangonomiya.app.extension.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sangonomiya.app.core.Response;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 权限不足的拒绝策略
 * @author Dioxide.CN
 * @date 2023/3/1 19:29
 * @since 1.0
 */
@Component
public class AccessUnit implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(new ObjectMapper().writeValueAsString(Response.fail("权限不足")));
        out.flush();
        out.close();
    }

}
