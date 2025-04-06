package com.example.demo.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.security.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser
@Sql(scripts = "classpath:sql/import-sample.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql/clean-up.sql", executionPhase = AFTER_TEST_METHOD)
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
}
