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
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/home")
    public String clientHome(Model model, Authentication auth) {
        List<Order> orders = orderService.getOrdersForClient(auth.getName());
        model.addAttribute("orders", orders);
        model.addAttribute("username", auth.getName());
        return "client";
    }

    @GetMapping("/order/new")
    public String showCreateOrderForm(Model model) {
        model.addAttribute("order", new Order());
        return "client-order-form";
    }

    @PostMapping("/order")
    public String createOrder(@ModelAttribute Order order, Authentication auth) {
        orderService.createOrder(order, auth.getName());
        return "redirect:/client/home?success";
    }
}