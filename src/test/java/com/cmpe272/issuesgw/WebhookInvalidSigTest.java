package com.cmpe272.issuesgw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "WEBHOOK_SECRET=testsecret")  // ✅ Add this line
@AutoConfigureMockMvc
class WebhookInvalidSigTest {

  @Autowired
  MockMvc mvc;

  @Test
  void invalidSignature_401() throws Exception {
    mvc.perform(post("/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-GitHub-Event", "ping")
        .header("X-GitHub-Delivery", "test-123")
        .header("X-Hub-Signature-256",
            "sha256=deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef")
        .content("{\"zen\":\"Keep it logically awesome.\"}"))
      .andExpect(status().isUnauthorized());  // ✅ Expect 401 instead of 503
  }
}