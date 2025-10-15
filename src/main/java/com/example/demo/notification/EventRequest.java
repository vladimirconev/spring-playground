package com.example.demo.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(@NotNull @NotBlank String name, @NotNull @NotBlank String payload) {}
