package com.cmpe272.issuesgw.service;

import com.cmpe272.issuesgw.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class GitHubService {
  // ✅ removed 'final' so tests can inject a mock WebClient
  private WebClient client;
  private final String owner;
  private final String repo;

  public GitHubService(@Value("${GITHUB_TOKEN:}") String token,
                       @Value("${GITHUB_OWNER:}") String owner,
                       @Value("${GITHUB_REPO:}") String repo) {
    this.owner = owner;
    this.repo = repo;
    this.client = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("Accept", "application/vnd.github+json")
            .defaultHeader("Authorization", (token == null || token.isBlank()) ? "" : "Bearer " + token)
            .build();
  }

  // ✅ new: setter to allow injecting a test WebClient
  public void setWebClient(WebClient webClient) {
    this.client = webClient;
  }

  public IssueResponse createIssue(IssueRequest req) {
    Map<String,Object> payload = new HashMap<>();
    payload.put("title", req.getTitle());
    if (req.getBody() != null) payload.put("body", req.getBody());
    if (req.getLabels() != null) payload.put("labels", req.getLabels());
    return client.post()
        .uri("/repos/{owner}/{repo}/issues", owner, repo)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(payload))
        .retrieve()
        .bodyToMono(IssueResponse.class)
        .block();
  }

  public List<IssueResponse> listIssues(String state, String labels, Integer page, Integer perPage, StringBuilder linkHeaderOut) {
    ClientResponse resp = client.get()
        .uri(uri -> uri.path("/repos/{owner}/{repo}/issues")
            .queryParam("state", state == null ? "open" : state)
            .queryParamIfPresent("labels", Optional.ofNullable(labels).filter(s -> !s.isBlank()))
            .queryParam("page", page == null ? 1 : page)
            .queryParam("per_page", perPage == null ? 30 : Math.min(perPage, 100))
            .build(owner, repo))
        .exchange()
        .block();

    if (resp == null) throw new RuntimeException("No response from GitHub");

    String link = resp.headers().asHttpHeaders().getFirst("Link");
    if (link != null && linkHeaderOut != null) {
      linkHeaderOut.append(link);
    }

    return resp.bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<IssueResponse>>() {})
        .block();
  }

  public IssueResponse getIssue(int number) {
    return client.get()
        .uri("/repos/{owner}/{repo}/issues/{n}", owner, repo, number)
        .retrieve()
        .bodyToMono(IssueResponse.class)
        .block();
  }

  public IssueResponse updateIssue(int number, String title, String body, String state) {
    Map<String,Object> patch = new HashMap<>();
    if (title != null) patch.put("title", title);
    if (body != null) patch.put("body", body);
    if (state != null) patch.put("state", state);

    return client.patch()
        .uri("/repos/{owner}/{repo}/issues/{n}", owner, repo, number)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(patch))
        .retrieve()
        .bodyToMono(IssueResponse.class)
        .block();
  }

  public Map<String,Object> addComment(int number, CommentRequest req) {
    return client.post()
        .uri("/repos/{owner}/{repo}/issues/{n}/comments", owner, repo, number)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(Map.of("body", req.getBody())))
        .retrieve()
        .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String,Object>>() {})
        .block();
  }
}