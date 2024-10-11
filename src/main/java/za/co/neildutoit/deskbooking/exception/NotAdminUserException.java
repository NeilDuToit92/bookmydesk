package za.co.neildutoit.deskbooking.exception;

public class NotAdminUserException extends RuntimeException {

  public NotAdminUserException(String message) {
    super(message);
  }
}
