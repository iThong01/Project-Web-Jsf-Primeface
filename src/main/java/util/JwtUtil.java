package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

 public class JwtUtil {
    private static final String SECRET_STR = "green-market-very-secret-key-2024";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STR.getBytes());
    private static final long EXPIRATION_TIME = 60*60*24*7;
    public static String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username) 
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }
    public static Claims validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}