package com.shopping.cart.service;

import com.shopping.cart.dto.request.AddToCartRequest;
import com.shopping.cart.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(String userId);

    CartResponse addToCart(String userId, AddToCartRequest request);

    CartResponse updateCartItem(String userId, String itemId, Integer quantity);

    CartResponse removeFromCart(String userId, String itemId);

    void clearCart(String userId);
}
