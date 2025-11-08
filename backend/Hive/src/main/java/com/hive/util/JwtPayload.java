package com.hive.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.function.Supplier;

public class JwtPayload {
    private final Claims claims;

    public JwtPayload(String token, Supplier<Key> signingKey) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BadCredentialsException("JWT not found!");
        }
        this.claims = Jwts
                .parser().verifyWith((SecretKey) signingKey.get())
                .build()
                .parseSignedClaims(token.substring(7)).getPayload();
    }

    public boolean isValid(UserDetails userDetails) {
        return username().equals(userDetails.getUsername()) && !expired();
    }

    public Long id() {
        return claims.get("id", Long.class);
    }

    public String username() {
        return claims.get("username", String.class);
    }

    public String role() {
        return claims.get("role", String.class);
    }

    public boolean expired() {
        return claims.getExpiration().before(new Date());
    }
}
