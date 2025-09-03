package com.example.demo.security;

import com.example.demo.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class LoginAuthFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final JWTService jwtService;
  private final RequestMatcher requireAuthenticationRequestMatcher;
  private final ObjectMapper objectMapper;

  public LoginAuthFilter(
      AuthenticationManager authenticationManager,
      final UserService userService,
      final JWTService jwtService,
      final ObjectMapper objectMapper) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.userService = userService;
    this.requireAuthenticationRequestMatcher =
        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/login");
    this.objectMapper = objectMapper;
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult) {
    final String username = authResult.getName();
    var user = userService.findByUsername(username);

    var token =
        jwtService.generateToken(
            "AUTH_TOKEN",
            Collections.singletonMap("username", username),
            Instant.now().plusMillis(4L * 3600000), // 4 hours
            Instant.now());
    var defaultCredentialsAuthToken =
        new CredentialsAuthToken(user.getEmail(), user.getId(), token, user.getAuthorities());
    response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    try (var outputStream = response.getOutputStream()) {
      objectMapper.writeValue(outputStream, defaultCredentialsAuthToken);
    } catch (IOException ex) {
      throw new RuntimeException(
          "Authentication failed due to: %s.".formatted(ex.getMessage()), ex);
    }
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    Authentication authentication = null;
    try (var inputStream = request.getInputStream()) {
      var loginRequest = objectMapper.readValue(inputStream, LoginRequest.class);
      var authenticationToken =
          new UserAuthToken(loginRequest.username(), loginRequest.password(), null);
      authentication = authenticationManager.authenticate(authenticationToken);
    } catch (final Exception ex) {
      response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
    return authentication;
  }

  @Override
  protected boolean requiresAuthentication(
      HttpServletRequest request, HttpServletResponse response) {
    return this.requireAuthenticationRequestMatcher.matches(request);
  }
}
