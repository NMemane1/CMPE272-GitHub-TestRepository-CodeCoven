package com.cmpe272.issuesgw.service;

import com.cmpe272.issuesgw.dto.CommentRequest;
import com.cmpe272.issuesgw.dto.IssueRequest;
import com.cmpe272.issuesgw.dto.IssueResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GitHubServiceHappyPathTest {

  private static WebClient webClientResponding(Map<String, ClientResponse> routes) {
    ExchangeFunction fx = request -> {
      String key = request.method().name() + " " + request.url().getPath();
      ClientResponse resp = routes.getOrDefault(
          key, ClientResponse.create(HttpStatus.NOT_FOUND).build());
      return reactor.core.publisher.Mono.just(resp);
    };
    return WebClient.builder().exchangeFunction(fx).baseUrl("https://api.github.com").build();
  }

  private static ClientResponse json(HttpStatus status, String body, HttpHeaders headers) {
    ClientResponse.Builder b = ClientResponse.create(status)
        .header("Content-Type", "application/json");
    if (headers != null) {
      headers.forEach((k, v) -> v.forEach(val -> b.header(k, val)));
    }
    if (body != null) {
      DataBuffer db = new DefaultDataBufferFactory()
          .wrap(body.getBytes(StandardCharsets.UTF_8));
      b.body(Flux.just(db));
    }
    return b.build();
  }

  @Test
  void create_get_update_comment_happy_flow() {
    String createdIssue = """
      {"number":42,"html_url":"https://github.com/o/r/issues/42",
       "state":"open","title":"t","body":"b","labels":[{"name":"test"}],
       "created_at":"2024-01-01T00:00:00Z","updated_at":"2024-01-01T00:00:00Z"}""";

    String gotIssue = """
      {"number":42,"html_url":"https://github.com/o/r/issues/42",
       "state":"open","title":"t2","body":"b2","labels":[{"name":"x"}],
       "created_at":"2024-01-01T00:00:00Z","updated_at":"2024-01-02T00:00:00Z"}""";

    String updatedIssue = """
      {"number":42,"html_url":"https://github.com/o/r/issues/42",
       "state":"closed","title":"t3","body":"b3","labels":[{"name":"y"}],
       "created_at":"2024-01-01T00:00:00Z","updated_at":"2024-01-03T00:00:00Z"}""";

    String commentResp = """
      {"id":999,"body":"hi","user":{"login":"me"},
       "created_at":"2024-01-04T00:00:00Z",
       "html_url":"https://github.com/o/r/issues/42#issuecomment-999"}""";

    HttpHeaders rl = new HttpHeaders();
    rl.add("X-RateLimit-Limit", "5000");
    rl.add("X-RateLimit-Remaining", "4999");

    WebClient wc = webClientResponding(Map.of(
        "POST /repos/o/r/issues",               json(HttpStatus.CREATED, createdIssue, rl),
        "GET /repos/o/r/issues/42",             json(HttpStatus.OK,      gotIssue,     rl),
        "PATCH /repos/o/r/issues/42",           json(HttpStatus.OK,      updatedIssue, rl),
        "POST /repos/o/r/issues/42/comments",   json(HttpStatus.CREATED, commentResp,  rl)
    ));

    GitHubService gh = new GitHubService("", "o", "r");
    gh.setWebClient(wc);

    IssueRequest createReq = new IssueRequest();
    createReq.setTitle("t");
    createReq.setBody("b");
    createReq.setLabels(List.of("test"));

    IssueResponse created = gh.createIssue(createReq);
    assertEquals(42, created.getNumber());
    assertEquals("open", created.getState());

    IssueResponse one = gh.getIssue(42);
    assertEquals("t2", one.getTitle());
    assertEquals("b2", one.getBody());

    IssueResponse upd = gh.updateIssue(42, "t3", "b3", "closed");
    assertEquals("closed", upd.getState());
    assertEquals("t3", upd.getTitle());

    CommentRequest c = new CommentRequest();
    c.setBody("hi");
    Map<String, Object> comment = gh.addComment(42, c);
    assertEquals(999, ((Number) comment.get("id")).intValue());
    assertEquals("hi", comment.get("body"));
  }
}
