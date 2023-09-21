package com.nahidio.RandomCodeGenerator.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;
import com.nahidio.RandomCodeGenerator.service.GenerationRequestService;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";  // This points to src/main/resources/templates/index.html (Thymeleaf template)
    }
}