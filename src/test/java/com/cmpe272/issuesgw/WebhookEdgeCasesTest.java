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

@SpringBootTest(properties = { "WEBHOOK_SECRET=testsecret" })
@AutoConfigureMockMvc
class WebhookEdgeCasesTest {

  @Autowired MockMvc mvc;

  private static String sig(String secret, String body) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
    byte[] h = mac.doFinal(body.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte b : h) sb.append(String.format("%02x", b));
    return sb.toString();
  }

  @Test
  void missingEventHeader_400() throws Exception {
    String body = "{\"action\":\"ping\"}";
    String hash = sig("testsecret", body);

    mvc.perform(post("/webhook")
        .header("X-Hub-Signature-256","sha256=" + hash)
        .header("X-GitHub-Delivery","junit-missing-event")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isBadRequest());
  }

  @Test
  void unknownEvent_400() throws Exception {
    String body = "{\"action\":\"something\"}";
    String hash = sig("testsecret", body);

    mvc.perform(post("/webhook")
        .header("X-GitHub-Event","unknown_event")
        .header("X-GitHub-Delivery","junit-unknown")
        .header("X-Hub-Signature-256","sha256=" + hash)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isBadRequest());
  }

  @Test
  void idempotent_sameDeliveryTwice_204both() throws Exception {
    String body = "{\"action\":\"opened\",\"issue\":{\"number\":42}}";
    String hash = sig("testsecret", body);

    // 1st
    mvc.perform(post("/webhook")
        .header("X-GitHub-Event","issues")
        .header("X-GitHub-Delivery","dup-42")
        .header("X-Hub-Signature-256","sha256=" + hash)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isNoContent());

    // 2nd with same delivery id: should still be 204 (idempotent)
    mvc.perform(post("/webhook")
        .header("X-GitHub-Event","issues")
        .header("X-GitHub-Delivery","dup-42")
        .header("X-Hub-Signature-256","sha256=" + hash)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isNoContent());
  }
}
