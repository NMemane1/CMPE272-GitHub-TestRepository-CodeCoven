package com.cmpe272.issuesgw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "WEBHOOK_SECRET=testsecret")
@AutoConfigureMockMvc
class WebhookUnknownEvent400Test {
  @Autowired MockMvc mvc;

  private static String hmac256Hex(String secret, String body) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
    byte[] raw = mac.doFinal(body.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte b : raw) sb.append(String.format("%02x", b));
    return sb.toString();
  }

  @Test
  void unknownEvent_400() throws Exception {
    String body = "{}";
    String sig = hmac256Hex("testsecret", body);

    mvc.perform(post("/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-GitHub-Event", "definitely_not_supported")
        .header("X-GitHub-Delivery", "t-unknown-1")
        .header("X-Hub-Signature-256", "sha256=" + sig)
        .content(body))
      .andExpect(status().isBadRequest()); // Expect 400 Bad Request
  }
}
