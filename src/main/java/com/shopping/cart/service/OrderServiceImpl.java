package com.shopping.cart.service;

import com.shopping.cart.dto.request.CheckoutRequest;
import com.shopping.cart.dto.response.OrderResponse;
import com.shopping.cart.exception.BadRequestException;
import com.shopping.cart.exception.ResourceNotFoundException;
import com.shopping.cart.model.*;
import com.shopping.cart.model.enums.OrderStatus;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.OrderRepository;
import com.shopping.cart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse checkout(String userId, CheckoutRequest request) {
        log.info("Processing checkout for user: {}", userId);

        // Get user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        // Validate cart is not empty
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout with empty cart");
        }

        // Validate stock availability for all items
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", cartItem.getProductId()));

            if (!product.getAvailable()) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
        }

        // Create order items from cart items
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .price(cartItem.getPrice())
                        .quantity(cartItem.getQuantity())
                        .subtotal(cartItem.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        // Generate unique order number
        String orderNumber = generateOrderNumber();

        // Create order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .items(orderItems)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .paymentInfo(request.getPaymentInfo())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());

        // Update product stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", cartItem.getProductId()));

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
            log.info("Updated stock for product {}: {}", product.getName(), product.getStockQuantity());
        }

        // Clear cart
        cart.getItems().clear();
        cart.calculateTotals();
        cartRepository.save(cart);
        log.info("Cart cleared for user: {}", userId);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getOrderHistory(String userId) {
        log.info("Fetching order history for user: {}", userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return orderRepository.findByUserId(userId, sort).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        log.info("Fetching order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus status) {
        log.info("Updating order {} status to: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully");
        return mapToOrderResponse(updatedOrder);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%04d", (int) (Math.random() * 10000));
        String orderNumber = "ORD-" + timestamp + "-" + randomSuffix;

        // Ensure uniqueness
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            randomSuffix = String.format("%04d", (int) (Math.random() * 10000));
            orderNumber = "ORD-" + timestamp + "-" + randomSuffix;
        }

        return orderNumber;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .items(order.getItems())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getTotalItems())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .maskedCardNumber(order.getPaymentInfo().getMaskedCardNumber())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
