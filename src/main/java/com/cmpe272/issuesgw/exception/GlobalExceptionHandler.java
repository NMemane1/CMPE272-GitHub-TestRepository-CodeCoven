package com.cmpe272.issuesgw.exception;

import com.cmpe272.issuesgw.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> onValidation(MethodArgumentNotValidException ex){
    List<String> details = ex.getBindingResult().getFieldErrors().stream()
      .map(f-> f.getField()+": "+f.getDefaultMessage()).toList();
    return ResponseEntity.badRequest().body(new ErrorResponse("bad_request","Validation failed", details));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> onIllegalArg(IllegalArgumentException ex){
    return ResponseEntity.badRequest().body(new ErrorResponse("bad_request", ex.getMessage(), List.of()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> onRuntime(RuntimeException ex){
    String msg = ex.getMessage()==null? "server error" : ex.getMessage();
    HttpStatus status = msg.toLowerCase().contains("auth")? HttpStatus.UNAUTHORIZED : HttpStatus.SERVICE_UNAVAILABLE;
    return new ResponseEntity<>(new ErrorResponse("error", msg, List.of()), status);
  }
}
