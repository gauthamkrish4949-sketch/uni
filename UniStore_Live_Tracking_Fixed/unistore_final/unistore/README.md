# UniStore – College E-Commerce & Product Tracking System

UniStore is a Java Spring Boot academic project for a college stationery marketplace and print-service platform.

## Main features
- Student/admin login
- Flipkart-style product browsing with search and category filters
- Product detail pages and quantity selection
- User-specific shopping cart
- Stock validation and automatic inventory deduction
- Stationery checkout
- Campus document printing service (PDF/DOC/DOCX)
- Order history
- Visual product/order tracking: Placed → Confirmed → Packed → Ready → Completed
- Admin inventory management
- Admin order-status management
- Responsive aesthetic web interface
- H2 file database so demo data survives application restarts

## Demo accounts
- Student: `student` / `student123`
- Admin: `admin` / `admin123`

## Run
1. Open the project in VS Code/IntelliJ/Eclipse.
2. Ensure Java 17+ and Maven are installed.
3. Run: `mvn spring-boot:run`
4. Open: `http://localhost:8080`

## Project structure
`model` – Product, Cart, Order, User entities
`repository` – Spring Data JPA repositories
`controller` – Authentication, store, print and admin workflows
`templates` – Thymeleaf UI pages
`static/css` – responsive UI styling

## Note
This is an academic/demo system. Passwords are intentionally simple for demonstration; a production system should use Spring Security and password hashing, CSRF protection, persistent user registration, proper file storage and a real payment gateway.
