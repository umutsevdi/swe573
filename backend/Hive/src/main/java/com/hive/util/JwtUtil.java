package com.hive.util;

import com.hive.app.account.model.UserDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
@Log4j2
@RequiredArgsConstructor
public class JwtUtil {

    private static final long EXPIRATION_MS = 2 * 60 * 60 * 1000; // 2h
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.issuer}")
    private String issuer;

    public JwtPayload parseToken(String token) {
        return new JwtPayload(token, this::getSigningKey);
    }

    public String generateToken(UserDetails userDetails) {
        UserDTO user = (UserDTO) userDetails;
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .issuer(issuer)
                .claims(Map.of(
                                "role", user.getRole().getAuthority(),
                                "id", user.getId(),
                                "username", user.getUsername()
                        )
                )
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        if (secretKey.length() < 64) {
            secretKey += secretKey.repeat((64 - secretKey.length()) / secretKey.length());
        }
        byte[] keyBytes = Decoders.BASE64.decode(secretKey + secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

