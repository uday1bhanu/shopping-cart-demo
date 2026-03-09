package com.shopping.cart.service;

import com.shopping.cart.aspect.PerformanceMonitored;
import com.shopping.cart.dto.response.ProductResponse;
import com.shopping.cart.exception.ResourceNotFoundException;
import com.shopping.cart.model.Order;
import com.shopping.cart.model.OrderItem;
import com.shopping.cart.model.Product;
import com.shopping.cart.repository.OrderRepository;
import com.shopping.cart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");
        return productRepository.findAll(pageable)
                .map(this::mapToProductResponse);
    }

    @Override
    public ProductResponse getProductById(String productId) {
        log.info("Fetching product by ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return mapToProductResponse(product);
    }

    @Override
    public List<ProductResponse> searchProducts(String query) {
        log.info("Searching products with query: {}", query);
        return productRepository.searchProducts(query).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String productId, Product product) {
        log.info("Updating product with ID: {}", productId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setAvailable(product.getAvailable());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully: {}", updatedProduct.getId());

        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        log.info("Deleting product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productRepository.delete(product);
        log.info("Product deleted successfully: {}", productId);
    }

    @Override
    @PerformanceMonitored(thresholdMs = 3000, errorMessage = "We're having trouble loading recommendations right now. Please try again in a moment.")
    public List<ProductResponse> getTrendingProducts() {
        log.info("Fetching trending products based on order history");

        // Fetch all products
        List<Product> allProducts = productRepository.findAll();
        log.info("Loaded {} products for trending analysis", allProducts.size());

        // Calculate popularity score for each product
        Map<String, Integer> productPopularityMap = new HashMap<>();

        for (Product product : allProducts) {
            // Get all orders to count this product
            List<Order> allOrders = orderRepository.findAll();

            int popularityScore = 0;
            for (Order order : allOrders) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProductId().equals(product.getId())) {
                        popularityScore += item.getQuantity();
                    }
                }
            }

            productPopularityMap.put(product.getId(), popularityScore);
        }

        // Sort by popularity and get top 20
        List<Map.Entry<String, Integer>> sortedProducts = productPopularityMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toList());

        // Fetch product details for trending items
        List<ProductResponse> trendingProducts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedProducts) {
            Optional<Product> productOpt = productRepository.findById(entry.getKey());
            if (productOpt.isPresent()) {
                trendingProducts.add(mapToProductResponse(productOpt.get()));
            }
        }

        return trendingProducts;
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .stockQuantity(product.getStockQuantity())
                .available(product.getAvailable())
                .build();
    }
}
