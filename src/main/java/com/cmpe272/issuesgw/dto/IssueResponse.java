package com.cmpe272.issuesgw.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class IssueResponse {
  private int number;
  private String html_url;
  private String state;
  private String title;
  private String body;
  private java.util.List<java.util.Map<String, Object>> labels;
  private OffsetDateTime created_at;
  private OffsetDateTime updated_at;

  public int getNumber() { return number; }
  public void setNumber(int number) { this.number = number; }
  public String getHtml_url() { return html_url; }
  public void setHtml_url(String html_url) { this.html_url = html_url; }
  public String getState() { return state; }
  public void setState(String state) { this.state = state; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getBody() { return body; }
  public void setBody(String body) { this.body = body; }
  public java.util.List<java.util.Map<String, Object>> getLabels() { return labels; }
  public void setLabels(java.util.List<java.util.Map<String, Object>> labels) { this.labels = labels; }
  public OffsetDateTime getCreated_at() { return created_at; }
  public void setCreated_at(OffsetDateTime created_at) { this.created_at = created_at; }
  public OffsetDateTime getUpdated_at() { return updated_at; }
  public void setUpdated_at(OffsetDateTime updated_at) { this.updated_at = updated_at; }
}
