package com.example.demo.security;

import com.example.demo.user.UserService;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

  final Logger logger = LoggerFactory.getLogger(UserAuthenticationProvider.class);

  private final UserService userService;

  public UserAuthenticationProvider(final UserService userService) {
    this.userService = userService;
  }

  @Override
  public Authentication authenticate(final Authentication authentication)
      throws AuthenticationException {
    var username = authentication.getName();
    var password =
        Optional.ofNullable(authentication.getCredentials()).map(Object::toString).orElse("");
    var user = userService.findByUsername(username);
    if (user == null) {
      logger.debug("Cannot authenticate non-existing user {}.", username);
      throw new BadCredentialsException("Non-existing user");
    }
    if (!Objects.equals(user.getPassword(), BCrypt.hashpw(password, user.getSalt()))) {
      logger.debug("Failed authentication of user {}.", username);
      throw new BadCredentialsException("Bad credentials");
    }
    logger.debug("Successful authentication of user {}.", username);
    return new UserAuthToken(username, password, user.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return Objects.equals(authentication, UserAuthToken.class);
  }
}
