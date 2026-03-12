package com.example.autoservice.controller;

import com.example.autoservice.model.Order;
import com.example.autoservice.model.User;
import com.example.autoservice.service.OrderService;
import com.example.autoservice.service.CustomUserDetailsService; // предположим, создадим позже или используем репозиторий напрямую
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/moderator")
public class ModeratorController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private com.example.autoservice.repository.jpa.UserRepository userRepository;

    @GetMapping("/home")
    public String moderatorHome(Model model, Authentication auth) {
        List<Order> allOrders = orderService.getAllOrders();
        List<User> allUsers = userRepository.findAll(); // для полноты можно показывать пользователей
        model.addAttribute("orders", allOrders);
        model.addAttribute("users", allUsers);
        model.addAttribute("username", auth.getName());
        return "moderator";
    }

    @PostMapping("/order/{id}/update")
    public String updateOrder(@PathVariable Long id,
                              @RequestParam String status,
                              @RequestParam(required = false) String carBrand,
                              @RequestParam(required = false) String carNumber,
                              @RequestParam(required = false) String description) {
        Order order = orderService.getOrderById(id);
        if (carBrand != null && !carBrand.isEmpty()) order.setCarBrand(carBrand);
        if (carNumber != null && !carNumber.isEmpty()) order.setCarNumber(carNumber);
        if (description != null && !description.isEmpty()) order.setDescription(description);
        if (status != null && !status.isEmpty()) order.setStatus(status);
        orderService.updateOrder(order);
        return "redirect:/moderator/home";
    }

    @PostMapping("/order/{id}/delete")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "redirect:/moderator/home";
    }
}