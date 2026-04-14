package com.greenmarket.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

 public class CookieUtil {
public static void addCookie(HttpServletResponse res,
                              String name,
                              String value,
                              int maxAge,
                              boolean httpOnly) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    cookie.setHttpOnly(httpOnly);
    cookie.setSecure(isSecureEnvironment());
    cookie.setAttribute("SameSite", "Strict");
    res.addCookie(cookie);
}

public static void removeCookie(HttpServletResponse res, String name) {
    Cookie cookie = new Cookie(name, "");
    cookie.setPath("/");
    cookie.setMaxAge(0);
    cookie.setHttpOnly(true);
    cookie.setAttribute("SameSite", "Strict");
    res.addCookie(cookie);
}
    public static String getCookieValue(HttpServletRequest req, String name) {
        if (req.getCookies() == null) {
            return null;
        }
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private static boolean isSecureEnvironment() {
        String env = System.getProperty("app.environment", "development");
        return "production".equalsIgnoreCase(env);
    }
}