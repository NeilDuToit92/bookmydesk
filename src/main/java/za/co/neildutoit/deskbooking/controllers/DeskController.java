package za.co.neildutoit.deskbooking.controllers;

import za.co.neildutoit.deskbooking.dto.DeskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.neildutoit.deskbooking.service.DeskBookingService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/desk")
@RequiredArgsConstructor
public class DeskController {
  private final DeskBookingService deskBookingService;

  @RequestMapping
  public List<DeskDto> getDesksForDate(@RequestParam LocalDate date) {
    log.info("getDesks - date: {}", date);
    return deskBookingService.getDesksForLayout(date);
  }

  @RequestMapping(value = "/all")
  public List<DeskDto> getAllDesks() {
    log.info("getAllDesks");
    return deskBookingService.getAllDesks();
  }
}
