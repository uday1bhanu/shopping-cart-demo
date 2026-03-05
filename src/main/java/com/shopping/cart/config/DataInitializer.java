package com.shopping.cart.config;

import com.shopping.cart.model.Cart;
import com.shopping.cart.model.Product;
import com.shopping.cart.model.User;
import com.shopping.cart.model.enums.UserRole;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");

        initializeAdminUser();
        initializeProducts();

        log.info("Data initialization completed successfully!");
    }

    private void initializeAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        Set<UserRole> adminRoles = new HashSet<>();
        adminRoles.add(UserRole.ROLE_ADMIN);
        adminRoles.add(UserRole.ROLE_USER);

        User admin = User.builder()
                .username("admin")
                .email("admin@shopping.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .roles(adminRoles)
                .active(true)
                .build();

        User savedAdmin = userRepository.save(admin);
        log.info("Admin user created: {}", savedAdmin.getUsername());

        // Create cart for admin
        Cart adminCart = Cart.builder()
                .userId(savedAdmin.getId())
                .build();
        cartRepository.save(adminCart);
        log.info("Cart created for admin user");
    }

    private void initializeProducts() {
        if (productRepository.count() > 0) {
            log.info("Products already exist, skipping creation");
            return;
        }

        List<Product> products = new ArrayList<>();

        // Generate 300 products across various categories
        String[] categories = {"Electronics", "Clothing", "Books", "Home & Kitchen", "Sports & Outdoors", "Toys & Games", "Beauty & Personal Care", "Automotive"};
        String[] electronics = {"Laptop", "Smartphone", "Tablet", "Headphones", "Smart Watch", "Camera", "Speaker", "Monitor", "Keyboard", "Mouse"};
        String[] clothing = {"T-Shirt", "Jeans", "Jacket", "Sneakers", "Dress", "Hoodie", "Sweater", "Shorts", "Socks", "Hat"};
        String[] books = {"Fiction Novel", "Technical Book", "Biography", "Cookbook", "Self-Help Book", "History Book", "Science Fiction", "Mystery Novel", "Children's Book", "Comic Book"};
        String[] homeKitchen = {"Blender", "Coffee Maker", "Microwave", "Toaster", "Cookware Set", "Knife Set", "Vacuum Cleaner", "Air Fryer", "Mixer", "Food Processor"};
        String[] sports = {"Yoga Mat", "Dumbbells", "Resistance Bands", "Running Shoes", "Tennis Racket", "Basketball", "Bicycle", "Swimming Goggles", "Fitness Tracker", "Jump Rope"};
        String[] toys = {"Action Figure", "Board Game", "Puzzle", "Building Blocks", "Doll", "Remote Control Car", "Educational Toy", "Art Set", "Musical Instrument", "Outdoor Play Set"};
        String[] beauty = {"Shampoo", "Moisturizer", "Lipstick", "Perfume", "Face Mask", "Hair Dryer", "Electric Toothbrush", "Nail Polish", "Sunscreen", "Body Lotion"};
        String[] automotive = {"Car Phone Mount", "Dash Cam", "Tire Pressure Gauge", "Jump Starter", "Car Vacuum", "Seat Cover", "Floor Mats", "Air Freshener", "Car Charger", "Tool Kit"};

        String[][] productNames = {electronics, clothing, books, homeKitchen, sports, toys, beauty, automotive};

        int productIndex = 0;
        for (int categoryIdx = 0; categoryIdx < categories.length; categoryIdx++) {
            String category = categories[categoryIdx];
            String[] items = productNames[categoryIdx];

            // Generate approximately 37-38 products per category to reach 300
            int productsPerCategory = 38;

            for (int i = 0; i < productsPerCategory && productIndex < 300; i++) {
                String itemName = items[i % items.length];
                int variant = (i / items.length) + 1;
                String productName = itemName + (variant > 1 ? " " + variant : "");

                // Generate random-looking but consistent prices
                double basePrice = 10 + (categoryIdx * 50) + (i * 5);
                BigDecimal price = new BigDecimal(String.format("%.2f", basePrice + (Math.random() * 100)));

                // Generate stock quantity
                int stockQuantity = 10 + (int)(Math.random() * 90);

                products.add(Product.builder()
                        .name(productName)
                        .description("High-quality " + productName.toLowerCase() + " - " + category)
                        .price(price)
                        .category(category)
                        .imageUrl("https://via.placeholder.com/300x200?text=" + productName.replace(" ", "+"))
                        .stockQuantity(stockQuantity)
                        .available(true)
                        .build());

                productIndex++;
            }
        }

        productRepository.saveAll(products);
        log.info("Created {} products across {} categories", products.size(), categories.length);
    }
}
