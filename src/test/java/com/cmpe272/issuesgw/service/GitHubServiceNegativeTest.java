package com.cmpe272.issuesgw.service;

import com.cmpe272.issuesgw.dto.IssueRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import static org.junit.jupiter.api.Assertions.*;

class GitHubServiceNegativeTest {

  @Test
  void throwsWhenNoResponseFromGitHub() {
    GitHubService svc = new GitHubService("token", "owner", "repo");
    StringBuilder linkHeaderOut = new StringBuilder();
    // Expect runtime exception since we’re not mocking GitHub API
    assertThrows(RuntimeException.class, () -> {
      svc.listIssues("open", null, 1, 10, linkHeaderOut);
    });
  }

  @Test
  void handlesEmptyTitleInCreateIssue() {
    GitHubService svc = new GitHubService("", "owner", "repo");
    IssueRequest req = new IssueRequest();
    req.setTitle(""); // invalid title should cause API failure
    assertThrows(Exception.class, () -> {
      svc.createIssue(req);
    });
  }
}
