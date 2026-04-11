package com.rudra.sessionbased_task_tracker.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider {

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private static final String CLAIM_TYPE = "type";

    @Value("${app.jwt-refresh-secret}")
    private String jwtRefreshSecret;

    @Value("${app.jwt-access-secret}")
    private String jwtAccessSecret;

    @Value("${app.jwt-access-exp}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt-refresh-exp}")
    private long refreshTokenExpirationMs;

    private SecretKey accessSigningKey;
    private SecretKey refreshSigningKey;

    @PostConstruct
    private void init() {
        this.accessSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.refreshSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
    }

    private SecretKey getSigningKey(String tokenType) {
        return TOKEN_TYPE_REFRESH.equals(tokenType) ? refreshSigningKey : accessSigningKey;
    }

    public String generateAccessToken(Long userId) {
        return buildToken(userId, accessTokenExpirationMs, TOKEN_TYPE_ACCESS);
    }

    public String generateRefreshToken(Long userId) {
        return buildToken(userId, refreshTokenExpirationMs, TOKEN_TYPE_REFRESH);
    }

    private String buildToken(Long userId, long expirationMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .claim(CLAIM_TYPE, type)
                .signWith(getSigningKey(type))
                .compact();
    }

    public Long getUserIdFromAccessToken(String token) {
        return Long.parseLong(parseAccessClaims(token).getSubject());
    }

    public Long getUserIdFromRefreshToken(String token) {
        return Long.parseLong(parseRefreshClaims(token).getSubject());
    }

    public String getAccessTokenType(String token) {
        return parseAccessClaims(token).get(CLAIM_TYPE, String.class);
    }

    public String getRefreshTokenType(String token) {
        return parseRefreshClaims(token).get(CLAIM_TYPE, String.class);
    }

    /**
     * Validates signature and expiry only. Does NOT check token type —
     * callers must verify type explicitly via {@link #isAccessToken(String)}
     * or {@link #isRefreshToken(String)} before trusting the token for its purpose.
     */
    public boolean validateToken(String token) {
        try {
            parseAccessClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("JWT malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.debug("JWT signature invalid: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("JWT unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean isAccessToken(String token) {
        return validateToken(token) && TOKEN_TYPE_ACCESS.equals(getAccessTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        try {
            parseRefreshClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parses and validates the token in one shot. Returns the claims if valid,
     * or empty if invalid. Use this to avoid parsing the same token multiple times.
     */
    public Optional<Claims> parseAndValidate(String token) {
        try {
            return Optional.of(parseAccessClaims(token));
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException
                 | UnsupportedJwtException | IllegalArgumentException e) {
            log.debug("JWT invalid: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Long getUserIdFromClaims(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public String getTokenTypeFromClaims(Claims claims) {
        return claims.get(CLAIM_TYPE, String.class);
    }

    private Claims parseClaimsWithKey(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }



    private Claims parseAccessClaims(String token) {
        Claims claims = parseClaimsWithKey(token, accessSigningKey);

        String type = claims.get(CLAIM_TYPE, String.class);
        if (!TOKEN_TYPE_ACCESS.equals(type)) {
            throw new SignatureException("Not an access token");
        }

        return claims;
    }

    private Claims parseRefreshClaims(String token) {
        Claims claims = parseClaimsWithKey(token, refreshSigningKey);

        String type = claims.get(CLAIM_TYPE, String.class);
        if (!TOKEN_TYPE_REFRESH.equals(type)) {
            throw new SignatureException("Not a refresh token");
        }

        return claims;
    }
}