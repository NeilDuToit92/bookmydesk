package za.co.neildutoit.deskbooking.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import za.co.neildutoit.deskbooking.dto.MessageDto;
import za.co.neildutoit.deskbooking.exception.BookingException;
import za.co.neildutoit.deskbooking.exception.NotAdminUserException;

@ControllerAdvice
public class ControllerAdviceHandler {
  @ExceptionHandler(BookingException.class)
  public ResponseEntity<MessageDto> handleBookingException(BookingException e) {
    MessageDto messageDto = MessageDto.builder()
            .message(e.getMessage())
            .build();
    return new ResponseEntity<>(messageDto, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotAdminUserException.class)
  public ResponseEntity<MessageDto> handleNotAdminUserException(BookingException e) {
    MessageDto messageDto = MessageDto.builder()
            .message(e.getMessage())
            .build();
    return new ResponseEntity<>(messageDto, HttpStatus.BAD_REQUEST);
  }
}
