package com.example.demo.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JWTService {

  private final String tokenIssuer;
  private final SecretKey secretKey;

  public JWTService(
      final @Value("${jwt.issuer}") String tokenIssuer,
      final @Value("${jwt.sign.key}") CharSequence signingKey) {
    this.tokenIssuer = tokenIssuer;
    this.secretKey =
        new SecretKeySpec(signingKey.toString().getBytes(StandardCharsets.UTF_8), "HmacSHA512");
  }

  public String generateToken(
      final String subject,
      final Map<String, Object> claims,
      final Date expiration,
      final Date issuedAt) {
    return Jwts.builder()
        .claims(Jwts.claims().subject(subject).add(claims).build())
        .issuer(tokenIssuer)
        .id(UUID.randomUUID().toString())
        .issuedAt(issuedAt)
        .signWith(secretKey, Jwts.SIG.HS512)
        .expiration((expiration))
        .compact();
  }

  public boolean isExpired(final CharSequence token) {
    var isExpired = false;
    try {
      var parser = Jwts.parser().verifyWith(secretKey).build();
      parser.parse(token);
    } catch (ExpiredJwtException ex) {
      isExpired = true;
    }
    return isExpired;
  }

  public String getClaim(final String claim, final CharSequence token) {
    var jwtParser = Jwts.parser().verifyWith(secretKey).build();
    return (String) jwtParser.parseSignedClaims(token).getPayload().get(claim);
  }
}
