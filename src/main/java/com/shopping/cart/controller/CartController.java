package com.shopping.cart.controller;

import com.shopping.cart.dto.request.AddToCartRequest;
import com.shopping.cart.dto.request.UpdateCartItemRequest;
import com.shopping.cart.dto.response.CartResponse;
import com.shopping.cart.dto.response.MessageResponse;
import com.shopping.cart.model.User;
import com.shopping.cart.service.CartService;
import com.shopping.cart.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        User currentUser = userService.getCurrentUser();
        CartResponse cart = cartService.getCart(currentUser.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        User currentUser = userService.getCurrentUser();
        CartResponse cart = cartService.addToCart(currentUser.getId(), request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        User currentUser = userService.getCurrentUser();
        CartResponse cart = cartService.updateCartItem(currentUser.getId(), itemId, request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable String itemId) {
        User currentUser = userService.getCurrentUser();
        CartResponse cart = cartService.removeFromCart(currentUser.getId(), itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<MessageResponse> clearCart() {
        User currentUser = userService.getCurrentUser();
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Cart cleared successfully")
                .build());
    }
}
