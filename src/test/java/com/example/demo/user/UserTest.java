package com.example.demo.user;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(User.class)
        .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY, Warning.SURROGATE_KEY)
        .verify();
  }
}
