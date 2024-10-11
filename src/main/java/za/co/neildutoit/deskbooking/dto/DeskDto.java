package za.co.neildutoit.deskbooking.dto;

import lombok.Builder;
import lombok.Data;
import za.co.neildutoit.deskbooking.enums.DeskStatus;

import java.time.LocalDate;

@Data
@Builder
public class DeskDto {
  private long databaseId;
  private long displayId;
  private long x;
  private long y;
  private DeskStatus status;
  private String bookedBy;
  private LocalDate date;
  private Boolean bookedByCurrentUser;
}
