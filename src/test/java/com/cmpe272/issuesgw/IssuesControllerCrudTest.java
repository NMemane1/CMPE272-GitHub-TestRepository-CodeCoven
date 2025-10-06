package com.cmpe272.issuesgw;

import com.cmpe272.issuesgw.controller.IssuesController;
import com.cmpe272.issuesgw.dto.IssueResponse;
import com.cmpe272.issuesgw.exception.GlobalExceptionHandler;
import com.cmpe272.issuesgw.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({IssuesController.class, GlobalExceptionHandler.class})
class IssuesControllerCrudTest {

  @Autowired MockMvc mvc;
  @MockBean GitHubService gh;

  @Test
  void postIssue_201() throws Exception {
    IssueResponse r = new IssueResponse();
    r.setNumber(123);
    Mockito.when(gh.createIssue(any())).thenReturn(r);

    mvc.perform(post("/issues")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"title\":\"t\",\"body\":\"b\",\"labels\":[\"x\"]}"))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", "/issues/123"));
  }

  @Test
  void getIssue_200() throws Exception {
    IssueResponse r = new IssueResponse();
    r.setNumber(7);
    r.setTitle("ok");
    Mockito.when(gh.getIssue(7)).thenReturn(r);

    mvc.perform(get("/issues/7"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.number").value(7));
  }

  @Test
  void patchIssue_invalidState_400() throws Exception {
    mvc.perform(patch("/issues/9")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"state\":\"invalid\"}"))
      .andExpect(status().isBadRequest());
  }

 // @Test
 // void postComment_201() throws Exception {
 // Mockito.when(gh.createComment(eq(5), any())).thenReturn("{\"id\":1}");
 // mvc.perform(post("/issues/5/comments")
 //       .contentType(MediaType.APPLICATION_JSON)
   //     .content("{\"body\":\"hi\"}"))
     // .andExpect(status().isCreated());
 // }
}
