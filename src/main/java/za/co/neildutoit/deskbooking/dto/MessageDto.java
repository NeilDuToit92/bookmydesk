package za.co.neildutoit.deskbooking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDto {
  private String message;

  public MessageDto(String message) {
    this.message = message;
  }
}
