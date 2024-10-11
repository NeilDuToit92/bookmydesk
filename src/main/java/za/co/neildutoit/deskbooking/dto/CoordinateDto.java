package za.co.neildutoit.deskbooking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoordinateDto {
  private long x;
  private long y;
}
