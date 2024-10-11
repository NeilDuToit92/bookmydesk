package za.co.neildutoit.deskbooking.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.neildutoit.deskbooking.service.UserService;

@Slf4j
@RestController
@RequestMapping(value = "/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @RequestMapping("/test")
    public String test() {
        userService.checkAdminUser();
        return "OK";
    }


}
