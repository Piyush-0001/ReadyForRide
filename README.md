ReadyForRide — Vehicle Rental & Booking Platform
A full-stack vehicle rental portfolio project built with Java, Spring Boot, MySQL, and a vanilla HTML/CSS/JavaScript frontend. Customers can explore cars and bikes, register or log in, book available rental dates, and complete a Razorpay test-mode payment. Administrators can manage vehicle inventory through protected REST APIs.
Project status: Local-development MVP intended for learning and demonstration, not a production rental/payment service. Razorpay runs in test mode only.

Features
- Vehicle catalog: View cars and bikes, search, filter by category, and sort by price.
- Accounts: Customer registration and login; BCrypt password hashing; JWT-based authentication with Spring Security.
- Authorization: Public vehicle browsing, admin-restricted vehicle creation/updates/deactivation, authenticated booking and payment endpoints.
- Bookings: Server-side rental-price calculation and validation of rental dates. A transactional overlap query, paired with a pessimistic vehicle-row lock, rejects conflicting reservations.
- Payment: Razorpay test-order creation, checkout integration, server-side signature and captured-payment verification, and booking status updates.
- Error handling: Bean Validation, request/response DTOs, and a global @RestControllerAdvice for API errors.
Tech stack
Layer	Technologies
Backend	Java 21, Spring Boot 4.1.1, Spring MVC, Spring Security, Spring Data JPA, Hibernate
Authentication	JWT (Spring Security OAuth2 Resource Server), BCrypt
Database	MySQL
Payment	Razorpay Java SDK, Razorpay Checkout (test mode)
Frontend	HTML, CSS, JavaScript, Fetch API
Tools	Maven, Eclipse, VS Code / Live Server, Postman, MySQL Workbench


Project structure
ReadyForRide/
├── Frontend/
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── details.html
│   ├── payment.html
│   ├── confirmation.html
│   ├── script.js
│   ├── payment.js
│   ├── register.js
│   ├── navbar.js
│   └── images/
└── vehicle-rental/
    ├── pom.xml
    └── src/main/
        ├── java/com/jsp/vehicle_rental/
        │   ├── config/
        │   ├── controller/
        │   ├── dto/
        │   ├── entity/
        │   ├── exception/
        │   ├── repo/
        │   └── service/
        └── resources/application.properties
How the booking flow works
1. The customer registers or logs in and receives a JWT.
2. The frontend fetches vehicle inventory from the REST API.
3. The customer selects a vehicle and rental dates.
4. The backend identifies the user from the JWT, locks the vehicle record, checks overlapping active bookings, and computes the amount using the vehicle's database rate.
5. The booking is saved as PENDING_PAYMENT, with a 15-minute hold.
6. The backend creates a Razorpay test order; the frontend opens Razorpay Checkout.
7. The backend verifies the checkout signature and fetches payment details from Razorpay. A valid captured payment within the hold changes the booking to CONFIRMED; late payments may require PAYMENT_REVIEW.
Date convention: The end date is exclusive (e.g., December 5–8 is three rental days).
API overview
Method	Endpoint	Access	Purpose
POST	/api/auth/register	Public	Create a customer account
POST	/api/auth/login	Public	Log in and receive JWT
GET	/api/vehicles	Public	List active vehicles
GET	/api/vehicles/{id}	Public	Get vehicle details
POST	/api/vehicles	Admin	Add a vehicle
PUT	/api/vehicles/{id}	Admin	Update a vehicle
PATCH	/api/vehicles/{id}/deactivate	Admin	Deactivate a vehicle
POST	/api/bookings	Authenticated	Create a booking
GET	/api/bookings/{id}	Booking owner	Read own booking
POST	/api/payments/{bookingId}/order	Booking owner	Create Razorpay test order
POST	/api/payments/{bookingId}/verify	Booking owner	Verify test payment


Protected endpoints use the header Authorization: Bearer <JWT>.
Run locally
Prerequisites
- JDK 21
- MySQL server
- Maven (or use the included Maven wrapper)
- VS Code with Live Server, or another local static-file server
- Razorpay test-mode API credentials
1. Create the database
In MySQL Workbench:
CREATE DATABASE IF NOT EXISTS vehicle_rental_db;
For local development, Hibernate is configured with spring.jpa.hibernate.ddl-auto=update to create/update tables. Use a proper migration strategy before production deployment.
2. Configure environment variables
The backend's application.properties reads variables rather than committing passwords/keys:
Environment variable	Description
DB_USERNAME	MySQL username
DB_PASSWORD	MySQL password
APP_JWT_SECRET	Base64-encoded random JWT HMAC secret (at least 32 decoded bytes)
RAZORPAY_KEY_ID	Razorpay test-mode key ID
RAZORPAY_KEY_SECRET	Razorpay test-mode key secret


Set these in your IDE's run configuration or your local environment. Never commit actual secret values. On older Windows PowerShell, a secret can be generated with:
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)
3. Start the backend
From vehicle-rental/:
./mvnw spring-boot:run
On Windows CMD, use mvnw.cmd spring-boot:run, or run VehicleRentalApplication in Eclipse with the environment variables configured. API base URL: http://localhost:8080.
4. Start the frontend
Open the Frontend/ folder in VS Code, then launch index.html with Live Server (typically port 5500). Run the backend and frontend at the same time.

CORS: SecurityConfig.java contains explicit allowed frontend origins. If Live Server uses a different hostname or IP address, adjust the allowed origins for your local environment. Don't use wildcard origins indiscriminately in a deployed application.
5. Try a booking
- Register or log in through the frontend.
- Select a vehicle and available future dates.
- Proceed to booking and Razorpay test checkout.
- Verify the resulting booking status in the application's response or local database.
You can also exercise the APIs in Postman. The login response contains the bearer JWT needed for authenticated requests.
Security and limitations
- Passwords are stored using BCrypt; signing keys, payment secrets, and database credentials are supplied through environment variables.
- A database lock and overlap query protect the booking creation path, but dedicated concurrency/load tests are still recommended.
- Payment confirmation depends on the browser invoking the verification API. Razorpay webhooks, recovery/reconciliation, and refunds are not fully implemented.
- Immediate JWT revocation, production deployment configuration, and comprehensive automated tests remain future improvements.
- Do not use actual customer data or live payment keys for a portfolio demonstration.
