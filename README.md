# Shopping Cart Demo - Spring Boot Application

A complete end-to-end e-commerce shopping cart application built with Spring Boot 3.2 and MongoDB.

## Features

- **User Authentication & Authorization**
  - JWT-based authentication
  - User registration and login
  - Role-based access control (USER, ADMIN)
  - BCrypt password encryption

- **Product Management**
  - Browse product catalog
  - Search products by name or category
  - Admin can create, update, and delete products

- **Shopping Cart**
  - Add items to cart
  - Update item quantities
  - Remove items from cart
  - Clear entire cart
  - Real-time cart total calculation

- **Order Management**
  - Checkout with shipping address and payment information
  - View order history
  - Order status tracking
  - Admin can update order status

## Technology Stack

- **Backend**: Spring Boot 3.2.4
- **Frontend**: React 18.2.0
- **Database**: MongoDB 7.0
- **Security**: Spring Security + JWT
- **Web Server**: nginx (for serving React app)
- **Build Tool**: Maven (backend), npm (frontend)
- **Containerization**: Docker & Docker Compose
- **Java Version**: 17

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- MongoDB 7.0 (if running locally without Docker)

## Getting Started

### Running with Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd shopping-cart-demo
   ```

2. **Build and start all services**
   ```bash
   docker-compose up -d
   ```

3. **Verify services are running**
   ```bash
   docker-compose ps
   docker-compose logs -f app
   docker-compose logs -f frontend
   ```

4. **Access the application**
   - **Frontend UI**: http://localhost:3000
   - **Backend API**: http://localhost:8080
   - **API Health Check**: http://localhost:8080/actuator/health

5. **Stop services**
   ```bash
   docker-compose down
   ```

6. **Clean up (remove volumes)**
   ```bash
   docker-compose down -v
   ```

## Using the Web Interface

Once the application is running, open your browser and navigate to http://localhost:3000

### Quick Start Guide

1. **Register a New Account**
   - Click "Register" in the navigation bar
   - Fill in your details (username, email, password, first name, last name)
   - Click "Register" to create your account

2. **Login**
   - Click "Login" in the navigation bar
   - Enter your username and password
   - Click "Login"

3. **Browse Products**
   - After login, you'll see the products page with 300 available products
   - Use the search bar to find specific products
   - Use pagination to navigate through products (12 per page)

4. **Add Items to Cart**
   - Click "Add to Cart" on any product card
   - View your cart by clicking the cart icon in the navigation bar

5. **Manage Your Cart**
   - Click on the cart icon to view your cart
   - Update quantities using the +/- buttons
   - Remove items by clicking "Remove"
   - See the order summary with total amount

6. **Checkout**
   - Click "Proceed to Checkout" from your cart
   - Fill in your shipping address
   - Enter payment information
   - Click "Place Order" to complete your purchase

7. **View Order History**
   - Click "Orders" in the navigation bar
   - See all your past orders with status tracking
   - View order details including items and shipping address

### Running Locally

1. **Start MongoDB**
   ```bash
   docker run -d -p 27017:27017 --name mongodb mongo:7.0
   ```

2. **Build the application**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   ```bash
   java -jar target/shopping-cart-demo-1.0.0.jar
   ```

## API Endpoints

### Authentication (`/api/auth`)

- `POST /api/auth/signup` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token
- `GET /api/auth/me` - Get current user profile (authenticated)

### Products (`/api/products`)

- `GET /api/products` - Get all products (public, paginated)
- `GET /api/products/{id}` - Get product by ID (public)
- `GET /api/products/search?query={query}` - Search products (public)
- `POST /api/products` - Create product (admin only)
- `PUT /api/products/{id}` - Update product (admin only)
- `DELETE /api/products/{id}` - Delete product (admin only)

### Cart (`/api/cart`)

- `GET /api/cart` - Get user's cart (authenticated)
- `POST /api/cart/items` - Add item to cart (authenticated)
- `PUT /api/cart/items/{itemId}` - Update item quantity (authenticated)
- `DELETE /api/cart/items/{itemId}` - Remove item (authenticated)
- `DELETE /api/cart` - Clear cart (authenticated)

### Orders (`/api/orders`)

- `POST /api/orders/checkout` - Checkout and create order (authenticated)
- `GET /api/orders` - Get order history (authenticated)
- `GET /api/orders/{id}` - Get order details (authenticated)
- `PUT /api/orders/{id}/status` - Update order status (admin only)

## Usage Examples

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }'
```

### 3. Browse Products

```bash
curl http://localhost:8080/api/products
```

### 4. Add Item to Cart

```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "<product-id>",
    "quantity": 2
  }'
```

### 5. View Cart

```bash
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer <your-jwt-token>"
```

### 6. Checkout

```bash
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": {
      "fullName": "Test User",
      "addressLine1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA",
      "phone": "555-0123"
    },
    "paymentInfo": {
      "cardNumber": "4111111111111111",
      "cardType": "VISA",
      "expiryMonth": "12",
      "expiryYear": "2025",
      "cvv": "123"
    }
  }'
```

### 7. View Order History

```bash
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Default Admin Account

- **Username**: admin
- **Password**: admin123
- **Role**: ROLE_ADMIN

## Configuration

Key configuration properties in `application.yml`:

- **Server Port**: 8080
- **MongoDB Host**: localhost:27017 (mongodb:27017 in Docker)
- **MongoDB Database**: shopping_cart
- **JWT Expiration**: 24 hours

## Project Structure

```
shopping-cart-demo/
├── src/                        # Backend source code
│   └── main/
│       ├── java/com/shopping/cart/
│       │   ├── config/           # Configuration classes
│       │   ├── controller/       # REST controllers
│       │   ├── dto/             # Data transfer objects
│       │   ├── exception/       # Exception handling
│       │   ├── model/           # Domain entities
│       │   ├── repository/      # MongoDB repositories
│       │   ├── security/        # Security components
│       │   └── service/         # Business logic
│       └── resources/
│           ├── application.yml
│           └── application-docker.yml
├── frontend/                   # Frontend React application
│   ├── public/                 # Static assets
│   ├── src/
│   │   ├── components/         # React components
│   │   ├── context/           # React Context (Auth, Cart)
│   │   ├── pages/             # Page components
│   │   ├── services/          # API services
│   │   ├── styles/            # CSS stylesheets
│   │   ├── App.js             # Main App component
│   │   └── index.js           # Entry point
│   ├── nginx.conf             # nginx configuration
│   ├── Dockerfile             # Frontend Docker image
│   └── package.json           # npm dependencies
├── Dockerfile                  # Backend Docker image
├── docker-compose.yml         # Docker Compose orchestration
└── pom.xml                    # Maven configuration
```

## Development

### Building the Project

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Viewing Logs

```bash
# Application logs (local)
tail -f logs/application.log

# Docker logs
docker-compose logs -f app
```

## Troubleshooting

### MongoDB Connection Issues

- Ensure MongoDB is running: `docker-compose ps`
- Check MongoDB logs: `docker-compose logs mongodb`
- Verify connection string in `application.yml`

### Application Won't Start

- Check Java version: `java -version` (must be 17+)
- Verify port 8080 is not in use: `lsof -i :8080`
- Review application logs: `docker-compose logs app`

### JWT Token Expired

- JWT tokens expire after 24 hours
- Login again to get a new token

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.
