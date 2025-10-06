package com.cmpe272.issuesgw;

import com.cmpe272.issuesgw.controller.WebhookController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
public class WebhookControllerTest {

  @Autowired MockMvc mvc;
  static String SECRET = "testsecret";

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r){
    r.add("WEBHOOK_SECRET", () -> SECRET);
  }

  @Test
  void webhook_validSignature_returns204() throws Exception {
    String payload = "{\"zen\":\"Keep it logically awesome.\"}";
    String sig = "sha256=" + hmac(SECRET, payload);
    mvc.perform(post("/webhook")
      .contentType(MediaType.APPLICATION_JSON)
      .header("X-GitHub-Event", "ping")
      .header("X-GitHub-Delivery", "abc123")
      .header("X-Hub-Signature-256", sig)
      .content(payload))
      .andExpect(status().isNoContent());
  }

  @Test
  void webhook_invalidSignature_returns401() throws Exception {
    String payload = "{}";
    mvc.perform(post("/webhook")
      .contentType(MediaType.APPLICATION_JSON)
      .header("X-GitHub-Event", "ping")
      .header("X-GitHub-Delivery", "abc123")
      .header("X-Hub-Signature-256", "sha256=badsignature")
      .content(payload))
      .andExpect(status().isUnauthorized());
  }

  private static String hmac(String secret, String body) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
    byte[] out = mac.doFinal(body.getBytes());
    StringBuilder sb = new StringBuilder(out.length*2);
    for (byte b: out) sb.append(String.format("%02x", b));
    return sb.toString();
  }
}
