package com.cmpe272.issuesgw.controller;

import com.cmpe272.issuesgw.util.HmacVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

@RestController
public class WebhookController {
  @Value("${WEBHOOK_SECRET:}") private String secret;

  private final Deque<Map<String,Object>> events = new ArrayDeque<>();
  private static final int MAX_EVENTS = 100;

  @PostMapping("/webhook")
  public ResponseEntity<Void> webhook(@RequestHeader(value="X-Hub-Signature-256", required=false) String sig,
                                      @RequestHeader(value="X-GitHub-Delivery", required=false) String deliveryId,
                                      @RequestHeader(value="X-GitHub-Event", required=false) String event,
                                      @RequestBody String body){
    if (!HmacVerifier.isValid(body, sig, secret)){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (event == null || !(event.equals("issues") || event.equals("issue_comment") || event.equals("ping"))){
      return ResponseEntity.badRequest().build();
    }
    Map<String,Object> rec = Map.of(
      "id", deliveryId == null ? UUID.randomUUID().toString() : deliveryId,
      "event", event,
      "timestamp", OffsetDateTime.now().toString()
    );
    synchronized (events){
      events.addFirst(rec);
      while (events.size() > MAX_EVENTS) events.removeLast();
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/events")
  public java.util.List<Map<String,Object>> list(@RequestParam(defaultValue="10") int limit){
    synchronized (events){
      return events.stream().limit(Math.max(1, Math.min(100, limit))).toList();
    }
  }
}
