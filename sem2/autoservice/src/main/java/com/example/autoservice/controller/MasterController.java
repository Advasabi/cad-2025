package com.example.autoservice.controller;

import com.example.autoservice.model.Order;
import com.example.autoservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/master")
public class MasterController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/home")
    public String masterHome(Model model, Authentication auth) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("username", auth.getName());
        return "master";
    }

    @PostMapping("/order/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    Authentication auth) {
        orderService.updateOrderStatus(id, status, auth.getName());
        return "redirect:/master/home";
    }
}