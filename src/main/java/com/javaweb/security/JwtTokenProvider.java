package com.javaweb.security;

import com.javaweb.enums.AccountStatus;
import com.javaweb.model.dto.MyUserDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKey12345678901234567890123456789012}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    /**
     * Generate a comprehensive JWT token containing extended user information
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add custom claims for MyUserDetail
        if (userDetails instanceof MyUserDetail) {
            MyUserDetail myUserDetail = (MyUserDetail) userDetails;
            claims.put("id", myUserDetail.getId());
            claims.put("fullName", myUserDetail.getFullName());
            claims.put("email", myUserDetail.getEmail());
            claims.put("accountStatus", myUserDetail.getAccountStatus().name());

            // Add roles/authorities as a comma-separated string
            String authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            claims.put("roles", authorities);

            // Add any other user attributes you might need
            if (myUserDetail.getAvatarId() != null) {
                claims.put("avatarId", myUserDetail.getAvatarId());
            }
            if (myUserDetail.getArtistId() != null) {
                claims.put("artistId", myUserDetail.getArtistId());
            }
        }

        return createToken(claims, userDetails.getUsername());
    }

    // Extract user details from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    public String extractFullName(String token) {
        return extractAllClaims(token).get("fullName", String.class);
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public AccountStatus extractAccountStatus(String token) {
        String status = extractAllClaims(token).get("accountStatus", String.class);
        return status != null ? AccountStatus.valueOf(status) : null;
    }

    public String extractRoles(String token) {
        return extractAllClaims(token).get("roles", String.class);
    }

    public String extractAvatarId(String token) {
        return extractAllClaims(token).get("avatarId", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Standard JWT utility methods
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setHeader(Map.of(
                        "alg", "HS256",
                        "typ", "JWT"
                ))
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}