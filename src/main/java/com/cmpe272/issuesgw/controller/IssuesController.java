package com.cmpe272.issuesgw.controller;

import com.cmpe272.issuesgw.dto.*;
import com.cmpe272.issuesgw.service.GitHubService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/issues")
public class IssuesController {
  private final GitHubService gh;
  public IssuesController(GitHubService gh){ this.gh = gh; }

  @PostMapping
  public ResponseEntity<IssueResponse> create(@Valid @RequestBody IssueRequest req){
    IssueResponse created = gh.createIssue(req);
    HttpHeaders h = new HttpHeaders();
    h.setLocation(URI.create("/issues/" + created.getNumber()));
    return new ResponseEntity<>(created, h, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<IssueResponse>> list(@RequestParam(defaultValue = "open") String state,
                                                  @RequestParam(required = false) String labels,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(name="per_page", defaultValue = "30") Integer perPage){
    StringBuilder linkOut = new StringBuilder();
    List<IssueResponse> issues = gh.listIssues(state, labels, page, perPage, linkOut);
    HttpHeaders h = new HttpHeaders();
    if (linkOut.length()>0) h.add("Link", linkOut.toString());
    return new ResponseEntity<>(issues, h, HttpStatus.OK);
  }

  @GetMapping("/{number}")
  public IssueResponse get(@PathVariable int number){ return gh.getIssue(number); }

  @PatchMapping("/{number}")
  public IssueResponse patch(@PathVariable int number, @RequestBody Map<String,Object> body){
    String state = body.get("state") == null ? null : body.get("state").toString();
    if (state!=null && !(state.equals("open") || state.equals("closed"))){
      throw new IllegalArgumentException("state must be 'open' or 'closed'");
    }
    String title = body.get("title")==null? null : body.get("title").toString();
    String content = body.get("body")==null? null : body.get("body").toString();
    return gh.updateIssue(number, title, content, state);
  }

  @PostMapping("/{number}/comments")
  public ResponseEntity<Map<String,Object>> comment(@PathVariable int number, @Valid @RequestBody CommentRequest req){
    var created = gh.addComment(number, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }
}
