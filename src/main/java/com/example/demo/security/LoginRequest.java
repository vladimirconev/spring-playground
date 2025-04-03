package com.example.demo.security;

import java.io.Serializable;

public record LoginRequest(String username, String password) implements Serializable {}
