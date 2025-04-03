package com.example.demo.security;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class UserAuthToken extends AbstractAuthenticationToken {

  private final String username;
  private final String password;

  public UserAuthToken(
      String username, String password, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.username = username;
    this.password = password;
  }

  @Override
  public String getName() {
    return this.username;
  }

  @Override
  public Object getPrincipal() {
    return null;
  }

  @Override
  public Object getCredentials() {
    return this.password;
  }
}
