package com.example.demo.user;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  public UserService(final UserRepository userRepository, final RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  public User findByUsername(final String username) {
    var user = userRepository.findByEmail(username);
    Set<GrantedAuthority> authorities =
        user.getUserRoles().stream()
            .map(Role::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    user.setAuthorities(authorities);
    return user;
  }

  @Transactional
  public UserResponse create(
      final String email, final CharSequence password, final Set<UserRole> roles) {
    var user = new User();
    user.setEmail(email);
    var salt = BCrypt.gensalt();
    user.setSalt(salt);
    user.setPassword(BCrypt.hashpw(String.valueOf(password), salt));
    var userRoles =
        roles.stream()
            .map(role -> roleRepository.findById(role.name()))
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());
    user.setUserRoles(userRoles);

    var output = userRepository.saveAndFlush(user);
    return new UserResponse(output.getId(), output.getEmail(), output.getCreatedAt());
  }

  public UserResponse findById(final UUID id) {
    return userRepository
        .findById(id)
        .map(user -> new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt()))
        .orElseThrow(
            () -> new NoSuchElementException("User with Id %s was not found.".formatted(id)));
  }

  @Transactional
  public void delete(final UUID id) {
    userRepository.deleteById(id);
  }
}
