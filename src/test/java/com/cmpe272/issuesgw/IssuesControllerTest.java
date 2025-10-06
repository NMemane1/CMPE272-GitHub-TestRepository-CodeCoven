package com.cmpe272.issuesgw;

import com.cmpe272.issuesgw.controller.IssuesController;
import com.cmpe272.issuesgw.exception.GlobalExceptionHandler;
import com.cmpe272.issuesgw.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({IssuesController.class, GlobalExceptionHandler.class})
public class IssuesControllerTest {

  @Autowired MockMvc mvc;
  @MockBean GitHubService gh;

  @Test
  void createIssue_missingTitle_returns400() throws Exception {
    String body = "{\n  \"body\": \"desc only\"\n}";
    mvc.perform(post("/issues").contentType(MediaType.APPLICATION_JSON).content(body))
      .andExpect(status().isBadRequest())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.code").value("bad_request"));
  }
}
