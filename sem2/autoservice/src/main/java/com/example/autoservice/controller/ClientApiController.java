package com.example.autoservice.controller;

import com.example.autoservice.model.Order;
import com.example.autoservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@PreAuthorize("hasRole('CLIENT')")
public class ClientApiController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/my-orders")
    public List<Order> getMyOrders(Authentication auth) {
        return orderService.getOrdersForClient(auth.getName());
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order, Authentication auth) {
        return orderService.createOrder(order, auth.getName());
    }
}