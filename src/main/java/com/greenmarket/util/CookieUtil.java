package com.greenmarket.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

 public class CookieUtil {
    public static void addCookie(HttpServletResponse res, String name, String value, int maxAge, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(httpOnly);
        res.addCookie(cookie);
    }

    public static void removeCookie(HttpServletResponse res, String name) {
        addCookie(res, name, "", 0, true);
    }
    
    public static String getCookieValue(HttpServletRequest req, String name) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (c.getName().equals(name)) return c.getValue();
            }
        }
        return null;
    }
}