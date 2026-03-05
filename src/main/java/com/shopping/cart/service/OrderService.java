package com.shopping.cart.service;

import com.shopping.cart.dto.request.CheckoutRequest;
import com.shopping.cart.dto.response.OrderResponse;
import com.shopping.cart.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse checkout(String userId, CheckoutRequest request);

    List<OrderResponse> getOrderHistory(String userId);

    OrderResponse getOrderById(String orderId);

    OrderResponse updateOrderStatus(String orderId, OrderStatus status);
}
