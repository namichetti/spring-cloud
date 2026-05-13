# 🧩 Spring Boot Microservices Architecture

Microservices architecture built with **Spring Boot** using a hybrid **REST + Event-Driven** approach.

The project is composed of multiple decoupled services:

- **book-composite-service** → API aggregation and public REST endpoints
- **book-service** → Core domain logic and persistence
- **book-review-service** → Review management
- **book-recommendation-service** → Recommendation management
- **api** → Shared contracts, interfaces, and domain models
- **util** → Shared utilities, event models, and reusable components

The architecture is currently evolving with the integration of **Apache Kafka** and **RabbitMQ** for asynchronous event-driven communication.

## ⚙️ Technologies Used

- Java
- Spring Boot
- Spring WebFlux
- Spring Data JPA
- MongoDB
- Apache Kafka *(in progress)*
- RabbitMQ *(in progress)*
- Docker
- Docker Compose
- Maven

## 🚀 Features

- Non-blocking REST APIs
- Microservices architecture
- Synchronous and asynchronous communication
- Distributed persistence per service
- Event-driven architecture
- Horizontal scalability
- Full containerized environment with Docker
