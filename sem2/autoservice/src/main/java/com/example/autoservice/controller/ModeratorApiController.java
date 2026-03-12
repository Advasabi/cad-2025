package com.example.autoservice.controller;

import com.example.autoservice.model.Order;
import com.example.autoservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderator")
@PreAuthorize("hasRole('MODERATOR')")
public class ModeratorApiController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/orders/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id,
                                   @RequestParam String status) {
        return orderService.updateOrderStatus(id, status, "moderator"); // username можно получить из SecurityContext, упростим
    }

    @DeleteMapping("/orders/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "Order deleted";
    }
}