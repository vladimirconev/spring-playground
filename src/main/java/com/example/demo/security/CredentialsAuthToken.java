package com.example.demo.security;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class CredentialsAuthToken extends UsernamePasswordAuthenticationToken {

  private final UUID userId;
  private final String email;
  private final String token;

  public CredentialsAuthToken(
      String email, UUID userId, String token, Collection<? extends GrantedAuthority> authorities) {
    super(email, null, authorities);
    this.token = token;
    this.userId = userId;
    this.email = email;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getToken() {
    return token;
  }
}
