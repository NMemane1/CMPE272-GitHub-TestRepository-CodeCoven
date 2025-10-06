package com.cmpe272.issuesgw.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class IssueRequest {
  @NotBlank(message = "title is required")
  private String title;
  private String body;
  private List<String> labels;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getBody() { return body; }
  public void setBody(String body) { this.body = body; }
  public List<String> getLabels() { return labels; }
  public void setLabels(List<String> labels) { this.labels = labels; }
}
