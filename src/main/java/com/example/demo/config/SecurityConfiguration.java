package com.example.demo.config;

import com.example.demo.security.JWTService;
import com.example.demo.security.LoginAuthFilter;
import com.example.demo.security.RequestAuthenticationFilter;
import com.example.demo.security.UserAuthenticationProvider;
import com.example.demo.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

  private final JWTService jwtService;
  private final UserService userService;
  private final UserAuthenticationProvider userAuthenticationProvider;
  private final ObjectMapper objectMapper;

  public SecurityConfiguration(
      JWTService jwtService,
      UserService userService,
      UserAuthenticationProvider userAuthenticationProvider,
      ObjectMapper objectMapper) {
    this.jwtService = jwtService;
    this.userService = userService;
    this.userAuthenticationProvider = userAuthenticationProvider;
    this.objectMapper = objectMapper;
  }

  private CorsConfigurationSource corsConfigurationSource() {
    final var source = new UrlBasedCorsConfigurationSource();
    final var corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(
        List.of(
            "*")); // probably it would be nice to list all URL as allowing everything is bad idea
    corsConfig.setAllowedMethods(
        List.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.OPTIONS.name()));
    corsConfig.setAllowedHeaders(
        List.of(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
            HttpHeaders.CACHE_CONTROL));
    source.registerCorsConfiguration("/**", corsConfig.applyPermitDefaultValues());

    return source;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    return configure(httpSecurity, authenticationManager(httpSecurity)).build();
  }

  private AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
    var authenticationManagerBuilder =
        httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.authenticationProvider(userAuthenticationProvider);
    return authenticationManagerBuilder.build();
  }

  private HttpSecurity configure(
      final HttpSecurity httpSecurity, final AuthenticationManager authenticationManager)
      throws Exception {
    var loginAuthenticationFilter =
        new LoginAuthFilter(authenticationManager, userService, jwtService, objectMapper);
    var requestAuthenticationFilter =
        new RequestAuthenticationFilter(authenticationManager, userService, jwtService);
    return httpSecurity
        .cors(
            httpSecurityCorsConfigurer ->
                httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(
                        PathPatternRequestMatcher.withDefaults()
                            .matcher(HttpMethod.GET, "/v3/api-docs/**"),
                        PathPatternRequestMatcher.withDefaults()
                            .matcher(HttpMethod.GET, "/swagger-ui/**"),
                        PathPatternRequestMatcher.withDefaults()
                            .matcher(HttpMethod.GET, "/swagger-ui.html"),
                        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/info"),
                        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/status"),
                        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/health"))
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/users")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilter(loginAuthenticationFilter)
        .addFilter(requestAuthenticationFilter)
        .sessionManagement(
            httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
        .logout(
            httpSecurityLogoutConfigurer ->
                httpSecurityLogoutConfigurer
                    .logoutRequestMatcher(
                        PathPatternRequestMatcher.withDefaults().matcher("/api/v1/logout"))
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .clearAuthentication(true)
                    .logoutSuccessHandler(
                        new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
        .authenticationManager(authenticationManager);
  }
}
