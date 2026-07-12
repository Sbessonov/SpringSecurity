package com.example.SpringSecurity.configuration.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class JWTUtils {

    // Ключ шифрования для JWT
    private final String secretString = "omegaSecretStringYouShouldNotSeeThisOhNoYouDidItForget";


    private final SecretKey secretKey;

    // Время действия токена в миллисекундах (24 часа)
    private static final long EXPIRATION_TIME = 86400000;
    // 240 часов
    private static final long REFRESH_EXPIRATION_TIME = 864000000;

    public JWTUtils() {
        secretKey = new SecretKeySpec(
                Base64.getDecoder().decode(secretString), SignatureAlgorithm.HS256.getJcaName()
        );
    }

    /*Метод для генерации JWT токена на основе данных пользователя*/
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails, new HashMap<>(), EXPIRATION_TIME);
    }

    // Метод для генерации токена обновления (refresh token) с дополнительными данными
    public String generateRefreshToken(UserDetails userDetails) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return buildToken(userDetails, claims, REFRESH_EXPIRATION_TIME);
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Метод для извлечения имени пользователя из токена
    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        Claims claimsJws = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claimsTFunction.apply(claimsJws);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    private String buildToken(UserDetails userDetails, HashMap<String, Object> claims, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from((Instant.now().plusMillis(expirationTime))))
                .signWith(secretKey)
                .compact();
    }
}