package za.co.neildutoit.deskbooking.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.service.DeskBookingService;
import za.co.neildutoit.deskbooking.service.UserService;

@Controller
@RequiredArgsConstructor
public class PageController {

  private final UserService userService;

  @RequestMapping(path = {"/", "/home"})
  public String home(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name, @RequestParam(required = false) Boolean heatmap) {
    boolean isAdmin = false;
    User user = userService.getCurrentUser();
    if (user != null && user.isAdmin()) {
      isAdmin = true;
    }
    model.addAttribute("name", name);
    model.addAttribute("isAdmin",isAdmin);
    model.addAttribute("heatmap", Boolean.TRUE.equals(heatmap));
    return "home";
  }

  @RequestMapping(path = {"/help"})
  public String help(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name) {
    model.addAttribute("name", name);
    return "help";
  }

  @RequestMapping(path = {"/deskedit"})
  public String deskedit(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name) {
    User user = userService.getCurrentUser();
    if (user == null || !user.isAdmin()) {
      return "redirect:/home";
    }
    model.addAttribute("name", name);
    return "deskedit";
  }

  @RequestMapping(path = {"/reservations"})
  public String reservations(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name) {
    User user = userService.getCurrentUser();
    if (user == null || !user.isAdmin()) {
      return "redirect:/home";
    }
    model.addAttribute("name", name);
    return "reservations";
  }

  @RequestMapping(path = {"/users"})
  public String users(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name) {
    User user = userService.getCurrentUser();
    if (user == null || !user.isAdmin()) {
      return "redirect:/home";
    }
    model.addAttribute("name", name);
    return "users";
  }

  @RequestMapping(path = {"/bookingadmin"})
  public String bookingAdmin(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name) {
    User user = userService.getCurrentUser();
    if (user == null || !user.isAdmin()) {
      return "redirect:/home";
    }
    model.addAttribute("name", name);
    return "bookingadmin";
  }



  @RequestMapping(path = {"/admin"})
  public String admin(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name) {
    User user = userService.getCurrentUser();
    if (user == null || !user.isAdmin()) {
      return "redirect:/home";
    }
    model.addAttribute("name", name);
    return "admin";
  }

  @RequestMapping(path = {"/todo"})
  public String todo(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name) {
    User user = userService.getCurrentUser();
    if (user == null || !user.isAdmin()) {
      return "redirect:/home";
    }
    model.addAttribute("name", name);
    return "todo";
  }

  @RequestMapping("/forbidden")
  public String forbidden() {
    return "forbidden";
  }
}
