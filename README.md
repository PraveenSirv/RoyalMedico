**RoyalMedico**

RoyalMedico is a modern microservices-based healthcare system built using Spring Boot. The architecture is clean, modular, and scalable — perfect for hospitals, pharmacies, and clinics that want digital transformation done right.

This project includes individual services for everything from user management to billing, prescriptions, payments, and more.

---

**Microservices Included**

| Service Name            | Description                                  |
|-------------------------|----------------------------------------------|
| `admin-service`         | Manages admin functionalities                |
| `api-gateway`           | Central API Gateway (Zuul / Spring Gateway)  |
| `billing-service`       | Handles billing and invoicing                |
| `customer-service`      | Patient/customer registration & profiles     |
| `eureka-server`         | Service registry for microservices           |
| `inventory-service`     | Medicine stock and inventory control         |
| `notification-service`  | Sends email/SMS/app notifications            |
| `order-service`         | Handles medicine orders                      |
| `payment-service`       | Processes online payments                    |
| `prescription-service`  | Manages patient prescriptions                |

---

**How to Run the Project**

**Prerequisites**

- Java 17+
- Maven
- MySQL or PostgreSQL
- IDE (IntelliJ / Eclipse recommended)
- Postman (for testing APIs)

**Steps to Run**

1. Clone the Repository

```bash
git clone https://github.com/PraveenSirv/RoyalMedico.git
cd RoyalMedico
````

2. Start Eureka Server

```bash
cd eureka-server
./mvnw spring-boot:run
```

3. Start API Gateway

```bash
cd api-gateway
./mvnw spring-boot:run
```

4. Start All Services

Each service can be started independently like so:

```bash
cd [service-name]
./mvnw spring-boot:run
```

> Do this for all services: `admin-service`, `billing-service`, etc.

---

**Tech Stack**

* Java 17
* Spring Boot
* Spring Cloud (Eureka, Gateway)
* REST APIs
* MySQL / JPA
* Lombok
* Maven
* Microservices Architecture

---

**Project Structure**

RoyalMedico/
── admin-service/
── api-gateway/
── billing-service/
── customer-service/
── eureka-server/
── inventory-service/
── notification-service/
── order-service/
── payment-service/
── prescription-service/
── uploads/

---

**Screenshots**

Postman Api testing
<img width="1440" height="900" alt="Screenshot 2025-08-08 at 12 08 57 PM" src="https://github.com/user-attachments/assets/1306f644-12bb-418a-88a6-913a593eed4b" />


Sample Code
<img width="1440" height="900" alt="Screenshot 2025-08-08 at 12 08 43 PM" src="https://github.com/user-attachments/assets/3baa9643-e2bd-4159-b95b-5b82767be933" />

---

**Frontend Coming Soon**

---

**Contributing**

Wanna help make RoyalMedico better?

1. Fork the repo
2. Create a feature branch
3. Commit and push

---

**MIT License**

Copyright (c) 2025 Praveen Sirv

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction.

---

**Author**

Made with ❤️ by [Praveen Sirv](https://github.com/PraveenSirv)

