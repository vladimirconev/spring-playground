package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.security.JWTService;
import com.example.demo.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class DemoApplicationTests {

  @Autowired private ObjectMapper objectMapper;
  @Autowired private JWTService jwtService;
  @Autowired private UserService userService;

  @Test
  void contextLoads() {
    assertThat(objectMapper).isNotNull();
    assertThat(jwtService).isNotNull();
    assertThat(userService).isNotNull();
  }
}
