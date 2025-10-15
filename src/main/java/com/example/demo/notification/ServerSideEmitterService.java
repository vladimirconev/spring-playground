package com.example.demo.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ServerSideEmitterService {
  final Logger logger = LoggerFactory.getLogger(ServerSideEmitterService.class);
  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  public SseEmitter add(SseEmitter emitter) {
    this.emitters.add(emitter);
    emitter.onCompletion(
        () -> {
          logger.debug("on complete");
          this.emitters.remove(emitter);
        });
    emitter.onTimeout(
        () -> {
          logger.debug("ON timeout");
          emitter.complete();
          this.emitters.remove(emitter);
        });
    emitter.onError(
        ex -> {
          logger.debug("ON ERROR");
          emitter.completeWithError(ex);
          this.emitters.remove(emitter);
        });
    return emitter;
  }

  public void send(SseEmitter.SseEventBuilder eventBuilder) {
    List<SseEmitter> failedEmitters = new ArrayList<>();
    this.emitters.forEach(
        emitter -> {
          try {
            emitter.send(eventBuilder);
          } catch (IOException ex) {
            emitter.completeWithError(ex);
            failedEmitters.add(emitter);
          }
        });
    this.emitters.removeAll(failedEmitters);
  }
}
