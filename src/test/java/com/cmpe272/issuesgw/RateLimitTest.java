package com.cmpe272.issuesgw;

import com.cmpe272.issuesgw.controller.IssuesController;
import com.cmpe272.issuesgw.dto.IssueRequest;
import com.cmpe272.issuesgw.exception.GlobalExceptionHandler;
import com.cmpe272.issuesgw.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({IssuesController.class, GlobalExceptionHandler.class})
class RateLimitTest {

  @Autowired MockMvc mvc;
  @MockBean GitHubService gh;

  @Test
  void createIssue_whenGitHubRateLimited_returnsServiceUnavailable() throws Exception {
    var ex = WebClientResponseException.create(
        403, "Forbidden", null,
        ("{\"message\":\"API rate limit exceeded\"}").getBytes(), null);
    Mockito.when(gh.createIssue(Mockito.any(IssueRequest.class))).thenThrow(ex);

    mvc.perform(post("/issues")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"title\":\"RateLimitProbe\",\"body\":\"simulate RL\"}"))
      .andExpect(status().is5xxServerError());
  }
}