package com.nahidio.RandomCodeGenerator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";  // This points to src/main/resources/templates/index.html (Thymeleaf template)
    }
}