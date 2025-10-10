package com.example.demo.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.security.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser
@Sql(scripts = "classpath:sql/import-sample.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql/clean-up.sql", executionPhase = AFTER_TEST_METHOD)
@Testcontainers(disabledWithoutDocker = true)
@Import(TestcontainersConfiguration.class)
class UserRestControllerTest {

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  @Test
  void createUser() throws Exception {
    var userRequest =
        new UserRequest("urs.jacobssen@acme.com", "password12", Set.of(UserRole.CUSTOMER));
    var mvcResult =
        mockMvc
            .perform(
                post("/api/v1/users")
                    .content(objectMapper.writeValueAsString(userRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    var mockResponse = mvcResult.getResponse();
    assertThat(mockResponse).isNotNull();
    var response = objectMapper.readValue(mockResponse.getContentAsString(), UserResponse.class);
    assertThat(response).isNotNull();
    assertThat(response.id()).isNotNull();
    assertThat(response.timestamp()).isNotNull();
    assertEquals(userRequest.email(), response.email());

    var loginResult =
        mockMvc
            .perform(
                post("/api/v1/login")
                    .content(
                        objectMapper.writeValueAsString(
                            new LoginRequest(
                                userRequest.email(), String.valueOf(userRequest.password()))))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    var loginResponse = loginResult.getResponse();
    assertThat(loginResponse).isNotNull();
  }

  @Test
  @WithMockUser(authorities = {"ADMIN", "CUSTOMER"})
  void fetchById() throws Exception {
    var userIds =
        List.of(
            UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"),
            UUID.fromString("3e6b2928-413b-4792-9fa3-3c62c0de127e"));

    for (var id : userIds) {
      var mvcResult =
          mockMvc
              .perform(
                  get("/api/v1/users/%s".formatted(id)).contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andReturn();
      var response = mvcResult.getResponse();
      assertThat(response).isNotNull();
      var userResponse = objectMapper.readValue(response.getContentAsString(), UserResponse.class);
      assertEquals(id, userResponse.id());
    }
  }

  @Test
  @WithMockUser(authorities = {"ADMIN"})
  void deleteUser() throws Exception {
    var userId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    mockMvc
        .perform(
            delete("/api/v1/users/%s".formatted(userId)).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andReturn();
  }
}
