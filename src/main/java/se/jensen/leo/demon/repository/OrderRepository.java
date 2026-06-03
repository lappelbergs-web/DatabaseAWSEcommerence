package se.jensen.leo.demon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.jensen.leo.demon.model.Order;
import se.jensen.leo.demon.model.User;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    List<Order> findByUserUserId(Long userId);
}