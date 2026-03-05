package com.shopping.cart.service;

import com.shopping.cart.dto.request.AddToCartRequest;
import com.shopping.cart.dto.response.CartResponse;
import com.shopping.cart.exception.BadRequestException;
import com.shopping.cart.exception.ResourceNotFoundException;
import com.shopping.cart.model.Cart;
import com.shopping.cart.model.CartItem;
import com.shopping.cart.model.Product;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse getCart(String userId) {
        log.info("Fetching cart for user: {}", userId);
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(String userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}", userId);

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Check if product is available
        if (!product.getAvailable()) {
            throw new BadRequestException("Product is not available");
        }

        // Check if product has sufficient stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        // Check if item already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.calculateSubtotal();
            log.info("Updated existing cart item quantity: {}", existingItem.getQuantity());
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .id(UUID.randomUUID().toString())
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(request.getQuantity())
                    .build();
            newItem.calculateSubtotal();
            cart.getItems().add(newItem);
            log.info("Added new item to cart: {}", product.getName());
        }

        cart.calculateTotals();
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String userId, String itemId, Integer quantity) {
        log.info("Updating cart item {} for user: {}", itemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));

        // Validate stock
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProductId()));

        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        item.setQuantity(quantity);
        item.calculateSubtotal();

        cart.calculateTotals();
        Cart savedCart = cartRepository.save(cart);

        log.info("Cart item updated successfully");
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(String userId, String itemId) {
        log.info("Removing item {} from cart for user: {}", itemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));

        if (!removed) {
            throw new ResourceNotFoundException("Cart item", "id", itemId);
        }

        cart.calculateTotals();
        Cart savedCart = cartRepository.save(cart);

        log.info("Item removed from cart successfully");
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        cart.getItems().clear();
        cart.calculateTotals();
        cartRepository.save(cart);

        log.info("Cart cleared successfully");
    }

    private Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems())
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .build();
    }
}
