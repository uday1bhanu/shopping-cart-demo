package com.shopping.cart.dto.response;

import com.shopping.cart.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private String id;

    private String userId;

    private List<CartItem> items;

    private BigDecimal totalAmount;

    private Integer totalItems;
}
