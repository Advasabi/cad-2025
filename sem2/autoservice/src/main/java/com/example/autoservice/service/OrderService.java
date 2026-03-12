package com.example.autoservice.service;

import com.example.autoservice.model.Order;
import com.example.autoservice.model.User;
import com.example.autoservice.repository.jpa.OrderRepository;
import com.example.autoservice.repository.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public Order createOrder(Order order, String clientUsername) {
        User client = userRepository.findByUsername(clientUsername)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        order.setClient(client);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("NEW");
        return orderRepository.save(order);
    }

    public List<Order> getOrdersForClient(String clientUsername) {
        User client = userRepository.findByUsername(clientUsername)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return orderRepository.findByClientOrderByCreatedAtDesc(client);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateOrderStatus(Long orderId, String newStatus, String masterUsername) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // Для модератора: полное редактирование (можно добавить позже)
    public Order updateOrder(Order order) {
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}