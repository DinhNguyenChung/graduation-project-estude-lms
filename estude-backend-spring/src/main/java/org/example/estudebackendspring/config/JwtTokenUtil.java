package org.example.estudebackendspring.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime; // default app token expiry

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // existing: generate token for login (keeps current behavior)
    public String generateToken(String loginCode,Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(loginCode)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // NEW: generate reset-password token with custom short expiry (minutes)
    public String generateResetToken(String loginCode, long minutesValid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + minutesValid * 60 * 1000);

        return Jwts.builder()
                .setSubject(loginCode)
                .claim("type", "pwd_reset")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getLoginCodeFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }
    public Long getUserIdFromToken(String token) {
        return getAllClaimsFromToken(token).get("userId", Long.class);
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate generic token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // NEW: validate reset token specifically (type claim + expiry)
    public boolean validateResetToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String type = claims.get("type", String.class);
            if (!"pwd_reset".equals(type)) return false;
            // parser already validated expiry; so if parse succeeded, expiry ok
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
