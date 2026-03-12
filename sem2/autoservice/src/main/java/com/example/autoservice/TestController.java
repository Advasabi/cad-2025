package com.example.autoservice;  // ← тот же пакет что и главный класс

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.autoservice.model.Order;
import com.example.autoservice.repository.jpa.OrderRepository;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private OrderRepository repository;
    
    @GetMapping("/ping")
    public String ping() {
        return "API работает!";
    }
    
    @GetMapping("/all")
    public List<Order> getAll() {
        return repository.findAll();
    }
}