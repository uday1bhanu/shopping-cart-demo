package com.shopping.cart.repository;

import com.shopping.cart.model.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findByUserId(String userId, Sort sort);

    Optional<Order> findByOrderNumber(String orderNumber);

    Boolean existsByOrderNumber(String orderNumber);
}
