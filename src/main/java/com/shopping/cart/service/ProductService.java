package com.shopping.cart.service;

import com.shopping.cart.dto.response.ProductResponse;
import com.shopping.cart.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse getProductById(String productId);

    List<ProductResponse> searchProducts(String query);

    ProductResponse createProduct(Product product);

    ProductResponse updateProduct(String productId, Product product);

    void deleteProduct(String productId);

    List<ProductResponse> getTrendingProducts();
}
