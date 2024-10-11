package za.co.neildutoit.deskbooking.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.neildutoit.deskbooking.dto.UserDto;
import za.co.neildutoit.deskbooking.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @RequestMapping("/all")
  public List<UserDto> getAllUsers() {
    userService.checkAdminUser();
    return userService.getAllUsers();
  }
}
