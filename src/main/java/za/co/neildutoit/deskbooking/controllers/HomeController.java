package za.co.neildutoit.deskbooking.controllers;

import za.co.neildutoit.deskbooking.dto.DeskDto;
import za.co.neildutoit.deskbooking.service.DeskService;
import za.co.neildutoit.deskbooking.util.ThymeleafUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DeskService deskService;

    @RequestMapping(path = {"/", "/home"})
    public String home(final Model model, @AuthenticationPrincipal(expression = "claims['name']") String name) {
        model.addAttribute("name", name);
        return "home";
    }
}
