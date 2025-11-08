package com.hive.util;

import com.hive.app.account.model.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
@Log4j2
@RequiredArgsConstructor
public class JwtUtil {

    private static final long EXPIRATION_MS = 2 * 60 * 60 * 1000; // 2h
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.issuer}")
    private String issuer;

    public String getBearer(String token) throws BadCredentialsException {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BadCredentialsException("JWT not found!");
        }
        return token.substring(7);
    }

    public String generateToken(UserDetails userDetails) {
        UserDTO user = (UserDTO) userDetails;
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(user.getId() + ":" + user.getUsername())
                .issuer(issuer)
                .claim("role", user.getRole().getAuthority())
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isExpired(token);
    }

    public Long extractId(String token) {
        return Long.parseLong(parseClaims(token).getSubject().split(":")[0]);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject().split(":")[1];
    }

    public String extractRoles(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts
                .parser().verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }

    private Key getSigningKey() {
        if (secretKey.length() < 64) {
            secretKey += secretKey.repeat((64 - secretKey.length()) / secretKey.length());
        }
        byte[] keyBytes = Decoders.BASE64.decode(secretKey + secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
}
