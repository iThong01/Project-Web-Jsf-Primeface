package com.greenmarket.filter;

import com.greenmarket.util.CookieUtil;
import com.greenmarket.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class JwtFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());

        boolean isPublicPage = path.equals("/") ||
            path.startsWith("/login/") || 
            path.startsWith("/index.xhtml") || 
            path.startsWith("/shop/shop.xhtml") ||
            path.startsWith("jakarta.faces.resource") ||
            path.startsWith("/shop/manage.xhtml") ; // อนุญาติให้เข้าถึงได้ ขก.ล้อกอินบ่อย

        if (isPublicPage) {
               chain.doFilter(req, res);
            return;
           }
        
        String token = CookieUtil.getCookieValue(request, "AUTH_TOKEN");
        try {
            if (token != null) {
                var claims = JwtUtil.validateToken(token);
                String role =claims.get("role", String.class);
                // if(path.startsWith("/shop/manage.xhtml") && !"admin".equals(role)){      เข็คสิทธิ์ admin ปิดไว้ก่อน
                //     response.sendRedirect(request.getContextPath() + "/index.xhtml");
                //     return ;
                // }
                chain.doFilter(req, res);
            } else {
                response.sendRedirect(request.getContextPath() + "/login/login.xhtml");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/login/login.xhtml");
        }
    }
    private String getJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH_TOKEN".equals(cookie.getName())) return cookie.getValue();
            }
        }
        return null;
    }
}