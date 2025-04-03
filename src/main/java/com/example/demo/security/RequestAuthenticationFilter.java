package com.example.demo.security;

import com.example.demo.user.Role;
import com.example.demo.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class RequestAuthenticationFilter extends BasicAuthenticationFilter {

  private final UserService userService;
  private final JWTService jwtService;

  public RequestAuthenticationFilter(
      final AuthenticationManager authenticationManager,
      final UserService userService,
      final JWTService jwtService) {
    super(authenticationManager);
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith("Bearer")) {
      chain.doFilter(request, response);
      return;
    }

    var authenticationToken = getAuthenticationToken(authHeader);
    if (authenticationToken != null) {
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      chain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }
  }

  private UsernamePasswordAuthenticationToken getAuthenticationToken(final String token) {

    if (StringUtils.isBlank(token)) {
      return null;
    }
    var jwt = token.replace("Bearer ", "");
    var payloads = jwt.split("\\.");
    if (payloads.length != 3) {
      return null;
    }

    var username = jwtService.getClaim("username", jwt);
    if (username.isEmpty()) {

      return null;
    }

    var user = userService.findByUsername(username);

    if (user != null) {
      MDC.put("userId", user.getUsername());
      Set<GrantedAuthority> authorities =
          user.getUserRoles().stream()
              .map(Role::getName)
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toSet());
      user.setAuthorities(authorities);

      return new UsernamePasswordAuthenticationToken(
          user.getUsername(), user.getPassword(), user.getAuthorities());
    }
    return null; // implies no user was found and FORBIDDEN 403 needs to be served back
  }
}
