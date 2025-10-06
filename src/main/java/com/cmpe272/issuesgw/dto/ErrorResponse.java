package com.cmpe272.issuesgw.dto;

import java.util.List;

public class ErrorResponse {
  private String code;
  private String message;
  private java.util.List<String> details;

  public ErrorResponse() {}
  public ErrorResponse(String code, String message, java.util.List<String> details) {
    this.code = code; this.message = message; this.details = details;
  }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  public java.util.List<String> getDetails() { return details; }
  public void setDetails(java.util.List<String> details) { this.details = details; }
}
