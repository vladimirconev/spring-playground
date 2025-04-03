package com.example.demo.user;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, Instant timestamp) implements Serializable {}
