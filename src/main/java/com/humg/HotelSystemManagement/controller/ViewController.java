package com.humg.HotelSystemManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
