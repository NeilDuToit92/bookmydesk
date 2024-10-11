package za.co.neildutoit.deskbooking.controllers.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import za.co.neildutoit.deskbooking.exception.NotAdminUserException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotAdminUserException.class)
  public ResponseEntity<String> handleNotAdminUserException(NotAdminUserException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
  }
}
