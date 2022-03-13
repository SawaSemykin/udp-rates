package ru.otus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.service.QuotesHandler;

/**
 * @author Aleksandr Semykin
 */
@Controller
@RequiredArgsConstructor
public class QuotesController {

    private final QuotesHandler quotesHandler;

    @GetMapping({"/", "/elvls/"})
    public String getQuotes(Model model) {
        var elvls = quotesHandler.getElvls();
        model.addAttribute("elvls", elvls);
        return "index";
    }
}
