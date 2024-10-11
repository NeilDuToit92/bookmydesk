package za.co.neildutoit.deskbooking.dto;

import za.co.neildutoit.deskbooking.enums.DeskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BookingDto {
    private long databaseId;
    private long displayId;
    private long x;
    private long y;
    @Builder.Default
    private DeskStatus status = DeskStatus.AVAILABLE;
    private String bookedBy;
    @Builder.Default
    private LocalDate date = LocalDate.now();
}
