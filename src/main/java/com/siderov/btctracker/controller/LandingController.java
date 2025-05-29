package com.siderov.btctracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    /**
     * Начална страница – предлага Create New или Restore.
     * View файл: src/main/resources/templates/index.html
     */
    @GetMapping("/")
    public String landing() {
        return "index";
    }
}
