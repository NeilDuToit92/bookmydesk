package za.co.neildutoit.deskbooking.controllers;

import za.co.neildutoit.deskbooking.dto.DeskDto;
import za.co.neildutoit.deskbooking.service.DeskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/desk")
@RequiredArgsConstructor
public class DeskController {
    private final DeskService deskService;

    @RequestMapping
    public List<DeskDto> getDesks(@RequestParam(required = false) LocalDate date) {
        log.info("getDesks - date: {}", date);
        return deskService.getDesksForLayout(date);
    }
}
