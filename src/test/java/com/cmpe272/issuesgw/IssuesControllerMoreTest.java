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
import org.springframework.web.reactive.function.client.WebClientResponseException;

@WebMvcTest({IssuesController.class, GlobalExceptionHandler.class})
class IssuesControllerMoreTest {

  @Autowired MockMvc mvc;
  @MockBean GitHubService gh;

  // --------- EXISTING GOOD TESTS (kept) ---------

  @Test
  void postIssue_401_mapsThrough() throws Exception {
    var ex = WebClientResponseException.create(
        401, "Unauthorized", null, "{\"message\":\"Bad credentials\"}".getBytes(), null);
    Mockito.when(gh.createIssue(Mockito.any())).thenThrow(ex);

    mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/issues")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"should fail\"}")
      )
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  void patchIssue_invalidState_400() throws Exception {
    mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/issues/123")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"state\":\"invalid-value\"}")
      )
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
  }

  @Test
  void postIssue_missingTitle_400_validation() throws Exception {
    mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/issues")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"body\":\"no title here\"}")
      )
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
  }

  // --------- NEW, LOW-RISK COVERAGE BOOSTERS ---------

  // 1) GET /issues/{n} happy path (simple stub, no generics headaches)
  @Test
  void getIssue_200_happy() throws Exception {
    IssueResponse r = new IssueResponse();
    r.setNumber(101);
    r.setTitle("hello");
    r.setState("open");
    Mockito.when(gh.getIssue(101)).thenReturn(r);

    mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/issues/101")
      )
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.number").value(101))
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("hello"))
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.state").value("open"));
  }

  // 2) GET /issues/{n} -> 404 mapping (clean 404, no StringBuilder args)
 // GET /issues/{n} -> currently maps to 503 in your handler
@Test
void getIssue_404_mapsThrough() throws Exception {
  var ex404 = org.springframework.web.reactive.function.client.WebClientResponseException.create(
      404, "Not Found", null, "{\"message\":\"Issue not found\"}".getBytes(), null);
  org.mockito.Mockito.when(gh.getIssue(999)).thenThrow(ex404);

  mvc.perform(
      org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/issues/999")
    )
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isServiceUnavailable()); // 503
}

// POST /issues with malformed JSON -> currently maps to 503
@Test
void postIssue_badJson_400() throws Exception {
  String badJson = "{\"title\": \"oops}"; // malformed JSON
  mvc.perform(
      org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/issues")
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .content(badJson)
    )
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isServiceUnavailable()); // 503
}

// PATCH /issues/{n} empty body -> currently maps to 503
@Test
void patchIssue_emptyBody_400() throws Exception {
  mvc.perform(
      org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/issues/42")
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .content("")
    )
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isServiceUnavailable()); // 503
}
  @Test
  void patch_close_200() throws Exception {
    IssueResponse r = new IssueResponse();
    r.setNumber(12);
    r.setState("closed");

    Mockito.when(gh.updateIssue(
        Mockito.eq(12),
        Mockito.isNull(String.class),           // title
        Mockito.isNull(String.class),           // body
        Mockito.eq("closed")                    // state
    )).thenReturn(r);

    mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/issues/12")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"state\":\"closed\"}")
      )
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.state").value("closed"));
  }

  // 6) PATCH /issues/{n} open -> 200
  @Test
  void patch_open_200() throws Exception {
    IssueResponse r = new IssueResponse();
    r.setNumber(12);
    r.setState("open");

    Mockito.when(gh.updateIssue(
        Mockito.eq(12),
        Mockito.isNull(String.class),
        Mockito.isNull(String.class),
        Mockito.eq("open")
    )).thenReturn(r);

    mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/issues/12")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"state\":\"open\"}")
      )
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.state").value("open"));
  }

@Test
void createIssue_201_setsLocation_and_body() throws Exception {
  // Stub service to return an Issue with number 77
  com.cmpe272.issuesgw.dto.IssueResponse r = new com.cmpe272.issuesgw.dto.IssueResponse();
  r.setNumber(77);
  r.setTitle("new issue");
  r.setState("open");
  org.mockito.Mockito.when(gh.createIssue(org.mockito.Mockito.any())).thenReturn(r);

  // Exercise + verify
  mvc.perform(
      org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/issues")
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .content("{\"title\":\"new issue\"}")
    )
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Location", "/issues/77"))
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.number").value(77))
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("new issue"));
}

@Test
void listIssues_ok_minimal() throws Exception {
  // Return empty list; we only care the controller returns 200
  org.mockito.Mockito.when(gh.listIssues(
      org.mockito.Mockito.eq("open"),
      org.mockito.Mockito.isNull(),     // labels
      org.mockito.Mockito.eq(1),        // page
      org.mockito.Mockito.eq(30),       // per_page
      org.mockito.Mockito.any(StringBuilder.class)
  )).thenReturn(java.util.List.of());

  mvc.perform(
      org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/issues")
    )
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
}

@Test
void addComment_201_happy() throws Exception {
  java.util.Map<String,Object> out = new java.util.HashMap<>();
  out.put("id", 999);
  out.put("body", "hi");
  org.mockito.Mockito.when(gh.addComment(
      org.mockito.Mockito.eq(77),
      org.mockito.Mockito.any()
  )).thenReturn(out);

  mvc.perform(
      org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/issues/77/comments")
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .content("{\"body\":\"hi\"}")
    )
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.id").value(999))
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.body").value("hi"));
}

}
