package com.example.demo.user;

import com.example.demo.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Validated
class UserRestController {
  final Logger logger = LoggerFactory.getLogger(UserRestController.class);

  private final UserService userService;

  public UserRestController(final UserService userService) {
    this.userService = userService;
  }

  @Operation(
      summary = "Create a User",
      tags = {"Users"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "CREATED",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "503",
            description = "Service temporally unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      })
  @PostMapping
  public ResponseEntity<UserResponse> create(@RequestBody @Valid final UserRequest userRequest) {
    var userResponse =
        userService.create(userRequest.email(), userRequest.password(), userRequest.roles());
    return ResponseEntity.created(URI.create("/api/v1/users/%s".formatted(userResponse.id())))
        .body(userResponse);
  }

  @Operation(
      summary = "Find User by ID",
      tags = {"Users"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "503",
            description = "Service temporally unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','CONSUMER')")
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> findById(@PathVariable("id") UUID id) {
    var userResponse = userService.findById(id);
    return ResponseEntity.ok(userResponse);
  }

  @Operation(
      summary = "Delete an user by ID",
      tags = {"Users"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(
            responseCode = "503",
            description = "Service temporally unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
    userService.delete(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
