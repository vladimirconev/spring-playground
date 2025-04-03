package com.example.demo.exception;

import java.io.Serializable;
import java.time.Instant;

public record ErrorResponse(
    String status,
    int code,
    String message,
    String path,
    String httpMethod,
    String exception,
    Instant timestamp)
    implements Serializable {}
