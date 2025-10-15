package com.example.demo.notification;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
class NotificationRestController {

  private final ServerSideEmitterService serverSideEmitterService;

  public NotificationRestController(final ServerSideEmitterService serverSideEmitterService) {
    super();
    this.serverSideEmitterService = serverSideEmitterService;
  }

  @Operation(
      summary = "Subscribe to receive notifications",
      tags = {"Notifications"})
  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @PreAuthorize("hasAuthority('CUSTOMER')")
  public SseEmitter registerEmitter() {
    return serverSideEmitterService.add(new SseEmitter(60000L)); // 1minute timeout
  }

  @Operation(
      summary = "Produce dummy notifications",
      tags = {"Notifications"})
  @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @PreAuthorize("hasAuthority('CUSTOMER')")
  public void sendEvent(@RequestBody @Valid EventRequest eventRequest) {
    serverSideEmitterService.send(
        SseEmitter.event()
            .id(UUID.randomUUID().toString())
            .name(eventRequest.name())
            .data(eventRequest.payload()));
  }
}
