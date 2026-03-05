#!/bin/bash

# Shopping Cart API Test Script
# This script tests all major flows of the application

BASE_URL="http://localhost:8080/api"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "Shopping Cart API Test Script"
echo "======================================"
echo ""

# Test 1: Health Check
echo -e "${YELLOW}Test 1: Health Check${NC}"
curl -s http://localhost:8080/actuator/health | jq .
echo ""
echo ""

# Test 2: Register a new user
echo -e "${YELLOW}Test 2: User Registration${NC}"
SIGNUP_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }')
echo "$SIGNUP_RESPONSE" | jq .
echo ""
echo ""

# Test 3: Login
echo -e "${YELLOW}Test 3: User Login${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }')
echo "$LOGIN_RESPONSE" | jq .

# Extract JWT token
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
echo -e "${GREEN}Token extracted: ${TOKEN:0:20}...${NC}"
echo ""
echo ""

# Test 4: Get current user
echo -e "${YELLOW}Test 4: Get Current User${NC}"
curl -s -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""
echo ""

# Test 5: Browse Products
echo -e "${YELLOW}Test 5: Get All Products (Paginated)${NC}"
PRODUCTS_RESPONSE=$(curl -s -X GET "$BASE_URL/products?page=0&size=5" | jq .)
echo "$PRODUCTS_RESPONSE"

# Extract first product ID
PRODUCT_ID=$(echo "$PRODUCTS_RESPONSE" | jq -r '.content[0].id')
PRODUCT_NAME=$(echo "$PRODUCTS_RESPONSE" | jq -r '.content[0].name')
echo -e "${GREEN}First product: $PRODUCT_NAME (ID: $PRODUCT_ID)${NC}"
echo ""
echo ""

# Test 6: Get single product
echo -e "${YELLOW}Test 6: Get Product by ID${NC}"
curl -s -X GET "$BASE_URL/products/$PRODUCT_ID" | jq .
echo ""
echo ""

# Test 7: Search products
echo -e "${YELLOW}Test 7: Search Products${NC}"
curl -s -X GET "$BASE_URL/products/search?query=book" | jq .
echo ""
echo ""

# Test 8: Get cart (should be empty)
echo -e "${YELLOW}Test 8: Get Cart (Empty)${NC}"
curl -s -X GET "$BASE_URL/cart" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""
echo ""

# Test 9: Add item to cart
echo -e "${YELLOW}Test 9: Add Item to Cart${NC}"
CART_RESPONSE=$(curl -s -X POST "$BASE_URL/cart/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"productId\": \"$PRODUCT_ID\",
    \"quantity\": 2
  }")
echo "$CART_RESPONSE" | jq .

# Extract cart item ID
ITEM_ID=$(echo "$CART_RESPONSE" | jq -r '.items[0].id')
echo -e "${GREEN}Cart item ID: $ITEM_ID${NC}"
echo ""
echo ""

# Test 10: Add another product to cart
echo -e "${YELLOW}Test 10: Add Another Item to Cart${NC}"
SECOND_PRODUCT_ID=$(echo "$PRODUCTS_RESPONSE" | jq -r '.content[1].id')
curl -s -X POST "$BASE_URL/cart/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"productId\": \"$SECOND_PRODUCT_ID\",
    \"quantity\": 1
  }" | jq .
echo ""
echo ""

# Test 11: Update cart item quantity
echo -e "${YELLOW}Test 11: Update Cart Item Quantity${NC}"
curl -s -X PUT "$BASE_URL/cart/items/$ITEM_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3
  }' | jq .
echo ""
echo ""

# Test 12: View cart with updated items
echo -e "${YELLOW}Test 12: View Updated Cart${NC}"
curl -s -X GET "$BASE_URL/cart" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""
echo ""

# Test 13: Checkout
echo -e "${YELLOW}Test 13: Checkout${NC}"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/orders/checkout" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": {
      "fullName": "Test User",
      "addressLine1": "123 Main Street",
      "addressLine2": "Apt 4B",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA",
      "phone": "555-0123"
    },
    "paymentInfo": {
      "cardType": "VISA",
      "cardNumber": "4111111111111111",
      "expiryMonth": "12",
      "expiryYear": "2025",
      "cvv": "123"
    }
  }')
echo "$ORDER_RESPONSE" | jq .

ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.id')
ORDER_NUMBER=$(echo "$ORDER_RESPONSE" | jq -r '.orderNumber')
echo -e "${GREEN}Order created: $ORDER_NUMBER (ID: $ORDER_ID)${NC}"
echo ""
echo ""

# Test 14: View cart after checkout (should be empty)
echo -e "${YELLOW}Test 14: View Cart After Checkout (Should be Empty)${NC}"
curl -s -X GET "$BASE_URL/cart" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""
echo ""

# Test 15: Get order history
echo -e "${YELLOW}Test 15: Get Order History${NC}"
curl -s -X GET "$BASE_URL/orders" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""
echo ""

# Test 16: Get specific order
echo -e "${YELLOW}Test 16: Get Order by ID${NC}"
curl -s -X GET "$BASE_URL/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""
echo ""

# Test 17: Admin Login
echo -e "${YELLOW}Test 17: Admin Login${NC}"
ADMIN_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')
echo "$ADMIN_LOGIN_RESPONSE" | jq .

ADMIN_TOKEN=$(echo "$ADMIN_LOGIN_RESPONSE" | jq -r '.token')
echo -e "${GREEN}Admin token extracted${NC}"
echo ""
echo ""

# Test 18: Update order status (Admin only)
echo -e "${YELLOW}Test 18: Update Order Status (Admin)${NC}"
curl -s -X PUT "$BASE_URL/orders/$ORDER_ID/status?status=CONFIRMED" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
echo ""
echo ""

# Test 19: Create a new product (Admin only)
echo -e "${YELLOW}Test 19: Create New Product (Admin)${NC}"
NEW_PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "This is a test product created via API",
    "price": 99.99,
    "category": "Test",
    "imageUrl": "https://via.placeholder.com/300x200?text=Test+Product",
    "stockQuantity": 10,
    "available": true
  }')
echo "$NEW_PRODUCT_RESPONSE" | jq .

NEW_PRODUCT_ID=$(echo "$NEW_PRODUCT_RESPONSE" | jq -r '.id')
echo -e "${GREEN}New product created with ID: $NEW_PRODUCT_ID${NC}"
echo ""
echo ""

# Test 20: Update product (Admin only)
echo -e "${YELLOW}Test 20: Update Product (Admin)${NC}"
curl -s -X PUT "$BASE_URL/products/$NEW_PRODUCT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Test Product",
    "description": "This product has been updated",
    "price": 149.99,
    "category": "Test",
    "imageUrl": "https://via.placeholder.com/300x200?text=Updated+Product",
    "stockQuantity": 15,
    "available": true
  }' | jq .
echo ""
echo ""

# Test 21: Delete product (Admin only)
echo -e "${YELLOW}Test 21: Delete Product (Admin)${NC}"
curl -s -X DELETE "$BASE_URL/products/$NEW_PRODUCT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
echo ""
echo ""

echo -e "${GREEN}======================================"
echo "All tests completed!"
echo "======================================${NC}"
