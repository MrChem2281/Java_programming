package com.example.demo.jwt;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo.enums.TokenType;
import com.example.demo.model.Token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor

public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;

    public Token generateAccessToken(Map<String,Object> extraClaims, long duration, TemporalUnit durationType, UserDetails user){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryData = now.plus(duration, durationType);
        String token = Jwts.builder().setClaims(extraClaims)
        .setSubject(user.getUsername())
        .setIssuedAt(toDate(now))
        .setExpiration(toDate(expiryData))
        .signWith(decodeSecretKey(secret), SignatureAlgorithm.HS256).compact();

        log.info("Generated ACCESS token for user: {}, expires: {}", user.getUsername(), expiryData);

        return new Token(TokenType.ACCESS, token, expiryData, false, null);
    }
     public Token generateRefreshToken(long duration, TemporalUnit durationType, UserDetails user){
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryData = now.plus(duration, durationType);
        String token = Jwts.builder()
        .setSubject(user.getUsername())
        .setIssuedAt(toDate(now))
        .setExpiration(toDate(expiryData))
        .signWith(decodeSecretKey(secret), SignatureAlgorithm.HS256).compact();

        log.info("Generated REFRESH token for user: {}, expires: {}", user.getUsername(), expiryData);

        return new Token(TokenType.REFRESH, token, expiryData, false, null);
    }

    public LocalDateTime getExpiryDate(String token){
        return toLocalDateTime(extractClaim(token, Claims::getExpiration));
    }

    private LocalDateTime toLocalDateTime(Date date){
        return date.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();

    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        return claimsResolver.apply(extractAllClaims(token));
    }
    
    public String getUsername(String token){
        try {
            String username = extractClaim(token, Claims::getSubject);
            log.debug("Extracted username from token: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token){
        if(token == null) return false;
        try{
            Jwts.parserBuilder().setSigningKey(decodeSecretKey(secret)).build().parseClaimsJws(token);
            log.debug("Token validation successful");
            return true;
        }
        catch (JwtException ex){
            log.warn("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(decodeSecretKey(secret)).build().parseClaimsJwt(token).getBody();
    }


    private Key decodeSecretKey(String secret){
        try {
            byte[] decodeKey = Base64.getDecoder().decode(secret);
            return Keys.hmacShaKeyFor(decodeKey);
        } catch (IllegalArgumentException e) {
            log.error("Error decoding secret key: {}", e.getMessage());
            return Keys.hmacShaKeyFor(secret.getBytes());
        }
    }
    private Date toDate(LocalDateTime time){
        return Date.from(time.toInstant(ZoneOffset.UTC));
    }
}
