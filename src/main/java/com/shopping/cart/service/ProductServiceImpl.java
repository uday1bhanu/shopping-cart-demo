package com.shopping.cart.service;

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

    /**
     * INTENTIONALLY BUGGY IMPLEMENTATION - N+1 Query Problem
     *
     * This method demonstrates a classic database performance anti-pattern.
     * For each of the 300 products, it makes multiple separate database queries:
     * 1. Fetch all products (1 query)
     * 2. For EACH product, fetch ALL orders and count occurrences (300 queries)
     * 3. For EACH product, fetch product details again (300 more queries)
     *
     * Total: 1 + 300 + 300 = 601 database queries!
     * Expected execution time: 5-15+ seconds
     *
     * Proper implementation would use MongoDB aggregation pipeline with $lookup and $group
     */
    @Override
    public List<ProductResponse> getTrendingProducts() {
        long startTime = System.currentTimeMillis();
        int queryCount = 0;

        log.warn("⚠️ Starting TRENDING PRODUCTS query - This will be VERY SLOW! ⚠️");

        try {
            // Query 1: Fetch ALL products from database
            log.warn("Query #{}: Fetching ALL products from database...", ++queryCount);
            List<Product> allProducts = productRepository.findAll();
            log.warn("Loaded {} products. Now calculating popularity for each one individually...", allProducts.size());

            // Create a map to store product popularity scores
            Map<String, Integer> productPopularityMap = new HashMap<>();

            // THE BUG: For each product, fetch ALL orders and count occurrences
            // This creates a massive N+1 query problem!
            for (int i = 0; i < allProducts.size(); i++) {
                Product product = allProducts.get(i);

                // Query N+1: Fetch ALL orders for EACH product
                log.warn("Query #{}: Fetching ALL orders to count product '{}' (ID: {}) [{}/{}]",
                        ++queryCount, product.getName(), product.getId(), i + 1, allProducts.size());

                List<Order> allOrders = orderRepository.findAll();

                // Count how many times this product appears in orders
                int popularityScore = 0;
                for (Order order : allOrders) {
                    for (OrderItem item : order.getItems()) {
                        if (item.getProductId().equals(product.getId())) {
                            popularityScore += item.getQuantity();
                        }
                    }
                }

                productPopularityMap.put(product.getId(), popularityScore);

                // Simulate additional processing delay (network latency, query overhead)
                try {
                    Thread.sleep(10); // 10ms per product = 3 seconds for 300 products
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // THE BUG CONTINUES: Instead of using the products we already have,
            // fetch each product AGAIN from database
            List<ProductResponse> trendingProducts = new ArrayList<>();

            // Sort products by popularity
            List<Map.Entry<String, Integer>> sortedProducts = productPopularityMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(20)
                    .collect(Collectors.toList());

            for (Map.Entry<String, Integer> entry : sortedProducts) {
                // Query N+1 AGAIN: Fetch product details individually instead of reusing data!
                log.warn("Query #{}: Fetching product details for ID: {}", ++queryCount, entry.getKey());

                Optional<Product> productOpt = productRepository.findById(entry.getKey());
                if (productOpt.isPresent()) {
                    trendingProducts.add(mapToProductResponse(productOpt.get()));
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.error("════════════════════════════════════════════════════════════════");
            log.error("🔥 PERFORMANCE DISASTER DETECTED! 🔥");
            log.error("════════════════════════════════════════════════════════════════");
            log.error("Total execution time: {} ms ({} seconds)", duration, duration / 1000.0);
            log.error("Total database queries executed: {}", queryCount);
            log.error("Average time per query: {} ms", duration / queryCount);
            log.error("Products processed: {}", allProducts.size());
            log.error("════════════════════════════════════════════════════════════════");

            // If it takes more than 3 seconds, throw an exception with stack trace
            if (duration > 3000) {
                RuntimeException performanceException = new RuntimeException(
                    "Unable to load the recommendations!"
                );
                log.error("Unable to load the recommendations! Query performance degradation detected! " +
                         "Execution time: {} ms, Database queries executed: {}, Average query time: {} ms",
                         duration, queryCount, duration / queryCount, performanceException);
                throw performanceException;
            }

            return trendingProducts;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Error in getTrendingProducts after {} ms and {} queries",
                    endTime - startTime, queryCount, e);
            throw e;
        }
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
