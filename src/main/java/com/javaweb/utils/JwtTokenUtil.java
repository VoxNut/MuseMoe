package com.javaweb.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.enums.AccountStatus;
import com.javaweb.model.dto.UserDTO;

import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JwtTokenUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode extractClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            return null;
        }
    }


    public static boolean isTokenExpired(String token) {
        try {
            JsonNode claims = extractClaims(token);
            if (claims == null || !claims.has("exp")) {
                return true;
            }

            long expirationTimestamp = claims.get("exp").asLong() * 1000;
            return new Date().after(new Date(expirationTimestamp));
        } catch (Exception e) {
            return true;
        }
    }

    public static UserDTO extractUserFromToken(String token) {
        try {
            JsonNode claims = extractClaims(token);
            if (claims == null) {
                return null;
            }

            UserDTO user = new UserDTO();

            if (claims.has("id")) {
                user.setId(claims.get("id").asLong());
            }

            if (claims.has("sub")) {
                user.setUsername(claims.get("sub").asText());
            }

            if (claims.has("fullName")) {
                user.setFullName(claims.get("fullName").asText());
            }

            if (claims.has("email")) {
                user.setEmail(claims.get("email").asText());
            }

            if (claims.has("avatarId")) {
                user.setAvatarId(claims.get("avatarId").asText());
            }

            if (claims.has("artistId")) {
                user.setArtistId(claims.get("artistId").asLong());
            }

            // Parse account status
            if (claims.has("accountStatus")) {
                try {
                    user.setAccountStatus(AccountStatus.valueOf(claims.get("accountStatus").asText()));
                } catch (Exception e) {
                    user.setAccountStatus(AccountStatus.ACTIVE);
                }
            } else {
                user.setAccountStatus(AccountStatus.ACTIVE);
            }

            // Parse roles if available
            if (claims.has("roles")) {
                String roles = claims.get("roles").asText();
                Set<String> roleEntities = new HashSet<>();

                for (String role : roles.split(",")) {
                    try {
                        roleEntities.add(role);
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                user.setRoles(roleEntities);
            }

            // Set token expiration time
            if (claims.has("exp")) {
                long expirationTimestamp = claims.get("exp").asLong() * 1000;
                user.setTokenExpiration(new Date(expirationTimestamp));
            }

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}