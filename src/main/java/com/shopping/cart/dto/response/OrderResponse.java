package com.shopping.cart.dto.response;

import com.shopping.cart.model.Address;
import com.shopping.cart.model.OrderItem;
import com.shopping.cart.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;

    private String orderNumber;

    private String userId;

    private List<OrderItem> items;

    private BigDecimal totalAmount;

    private Integer totalItems;

    private OrderStatus status;

    private Address shippingAddress;

    private String maskedCardNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
