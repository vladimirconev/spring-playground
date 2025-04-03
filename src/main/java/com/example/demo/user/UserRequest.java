package com.example.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

public record UserRequest(
    @NotNull(message = "Email can not be null.")
        @NotBlank(message = "Email can not be blank.")
        @Email(message = "Provide valid email.")
        String email,
    @NotNull(message = "Password can not be null.")
        @NotBlank(message = "Password can not be blank.")
        CharSequence password,
    @NotNull(message = "Roles can not be null.") Set<UserRole> roles)
    implements Serializable {}
