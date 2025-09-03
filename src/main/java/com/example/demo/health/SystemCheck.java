package com.example.demo.health;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

@WebEndpoint(id = "status")
@Endpoint(id = "status")
@Component
@Profile("!test")
public class SystemCheck {

  private final HealthEndpoint healthEndpoint;

  public SystemCheck(final HealthEndpoint healthEndpoint) {
    this.healthEndpoint = healthEndpoint;
  }

  @ReadOperation
  public @ResponseBody ResponseEntity<HealthComponent> healthStatus() {
    var healthComponent = healthEndpoint.health();
    var httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
    if (healthComponent.getStatus().equals(Status.UP)) {
      httpStatus = HttpStatus.OK;
    }
    return new ResponseEntity<>(healthComponent, httpStatus);
  }
}
