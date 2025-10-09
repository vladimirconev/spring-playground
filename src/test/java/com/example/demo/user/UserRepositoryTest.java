package com.example.demo.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.*;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/import-sample.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:sql/clean-up.sql", executionPhase = AFTER_TEST_CLASS)
@Testcontainers(disabledWithoutDocker = true)
class UserRepositoryTest {

  @Autowired private RoleRepository roleRepository;

  @Test
  void findById() {
    final Optional<Role> roleAdmin = roleRepository.findById("ADMIN");
    final Optional<Role> roleCustomer = roleRepository.findById("CUSTOMER");

    final Optional<Role> roleNotExisting = roleRepository.findById("DUMMY_ROLE");

    Assertions.assertFalse(roleNotExisting.isPresent());
    Assertions.assertTrue(roleAdmin.isPresent());
    Assertions.assertTrue(roleCustomer.isPresent());
  }

  @Test
  void countCorrectNumberOfRoles() {
    final Integer rolesCount = roleRepository.findAll().size();

    assertEquals(2, rolesCount);
  }

  @Test
  void insertion() {
    var newRole = new Role();
    newRole.setName("SUBSIDIARY_ADMIN");
    var output = roleRepository.saveAndFlush(newRole);
    Assertions.assertNotNull(output);
    Assertions.assertNotNull(output.getCreatedAt());
    assertEquals(newRole.getName(), output.getName());
  }
}
