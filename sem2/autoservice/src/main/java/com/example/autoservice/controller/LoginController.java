package com.example.autoservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/default")
    public String redirectAfterLogin(Authentication auth) {
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            return "redirect:/client/home";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MASTER"))) {
            return "redirect:/master/home";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR"))) {
            return "redirect:/moderator/home";
        }
        return "redirect:/login";
    }
}