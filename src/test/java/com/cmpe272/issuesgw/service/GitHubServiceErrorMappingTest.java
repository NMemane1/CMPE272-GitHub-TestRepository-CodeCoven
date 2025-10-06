package com.cmpe272.issuesgw.service;

import com.cmpe272.issuesgw.dto.IssueRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitHubServiceErrorMappingTest {

  private static WebClient wc(HttpStatus status, String body, HttpHeaders headers, String key) {
    ExchangeFunction fx = req -> {
      String pathKey = req.method().name() + " " + req.url().getPath();
      ClientResponse.Builder b = ClientResponse.create(
          pathKey.equals(key) ? status : HttpStatus.NOT_FOUND
      ).header("Content-Type","application/json");
      if (headers != null) headers.forEach((k, v) -> v.forEach(val -> b.header(k, val)));
      if (body != null && pathKey.equals(key)) {
        DataBuffer db = new DefaultDataBufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8));
        b.body(Flux.just(db));
      }
      return reactor.core.publisher.Mono.just(b.build());
    };
    return WebClient.builder().exchangeFunction(fx).baseUrl("https://api.github.com").build();
  }

  @Test
  void getIssue_401_throwsWebClientResponseException() {
    String err = "{\"message\":\"Bad credentials\"}";
    WebClient web = wc(HttpStatus.UNAUTHORIZED, err, null, "GET /repos/o/r/issues/1");
    GitHubService gh = new GitHubService("", "o", "r");
    gh.setWebClient(web);
    assertThrows(WebClientResponseException.class, () -> gh.getIssue(1));
  }

  @Test
  void getIssue_404_throwsWebClientResponseException() {
    String err = "{\"message\":\"Not Found\"}";
    WebClient web = wc(HttpStatus.NOT_FOUND, err, null, "GET /repos/o/r/issues/999");
    GitHubService gh = new GitHubService("", "o", "r");
    gh.setWebClient(web);
    assertThrows(WebClientResponseException.class, () -> gh.getIssue(999));
  }

  @Test
  void createIssue_rateLimited_throwsAndMentionsRetry() {
    HttpHeaders rl = new HttpHeaders();
    rl.add("X-RateLimit-Remaining","0");
    rl.add("Retry-After","30");
    String err = "{\"message\":\"API rate limit exceeded\"}";
    WebClient web = wc(HttpStatus.FORBIDDEN, err, rl, "POST /repos/o/r/issues");

    GitHubService gh = new GitHubService("", "o", "r");
    gh.setWebClient(web);

    IssueRequest req = new IssueRequest();
    req.setTitle("t");
    req.setBody("b");
    req.setLabels(List.of());

    WebClientResponseException ex =
        assertThrows(WebClientResponseException.class, () -> gh.createIssue(req));

    String m = ex.getResponseBodyAsString().toLowerCase();
    assertTrue(m.contains("limit") || m.contains("retry"));
  }
}
