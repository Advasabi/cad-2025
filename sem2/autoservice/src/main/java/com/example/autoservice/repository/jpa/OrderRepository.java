package com.example.autoservice.repository.jpa;

import com.example.autoservice.model.Order;
import com.example.autoservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClient(User client);
    List<Order> findByStatus(String status);
    List<Order> findByClientOrderByCreatedAtDesc(User client);
    List<Order> findAllByOrderByCreatedAtDesc();
}