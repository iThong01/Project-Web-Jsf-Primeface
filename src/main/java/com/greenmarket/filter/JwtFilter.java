package com.greenmarket.filter;

import com.greenmarket.util.CookieUtil;
import com.greenmarket.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class JwtFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());

        if (isPublicPage(path)) {
            chain.doFilter(req, res);
            return;
        }

        String token = CookieUtil.getCookieValue(request, "AUTH_TOKEN");

        if (token == null || token.trim().isEmpty()) {
            response.sendRedirect(contextPath + "/login/login.xhtml");
            return;
        }

        try {
            Claims claims = JwtUtil.validateToken(token);
            String role = claims.get("role", String.class);

            if (path.startsWith("/shop/manage.xhtml") && !"admin".equals(role)) {
                response.sendRedirect(contextPath + "/index.xhtml");
                return;
            }

            chain.doFilter(req, res);

        } catch (Exception e) {
            response.sendRedirect(contextPath + "/login/login.xhtml");
        }
    }

    private boolean isPublicPage(String path) {
        return path.equals("/") ||
               path.startsWith("/login/") ||
               path.startsWith("/index.xhtml") ||
               path.startsWith("/shop/shop.xhtml") ||
               path.startsWith("/article/article.xhtml") ||
               path.startsWith("/jakarta.faces.resource") ||
               path.startsWith("/javax.faces.resource");
    }
}