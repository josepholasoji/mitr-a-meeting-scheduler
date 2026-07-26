# Metr - Meeting Scheduler (A backend coding challenge)

> A production-ready implementation of a high-performance meeting scheduling service inspired by Doodle, built with  **Java 21** ,  **Spring Boot** ,  **PostgreSQL** , and modern cloud-native engineering practices.

---

# Overview

This project implements a simplified version of a meeting scheduling platform similar to Doodle.

The objective of this implementation is **not** to recreate the complete Doodle platform, but rather to demonstrate backend engineering principles including:

* Clean Architecture
* Domain Driven Design (DDD)
* SOLID Principles
* RESTful API Design
* Scalability
* Observability
* Production Readiness
* Testing
* Dockerized Deployment

The implementation follows the requirements provided in the assignment while making deliberate architectural decisions aimed at maintainability, extensibility, and performance.

---

# Technology Stack

| Category            | Technology                        |
| ------------------- | --------------------------------- |
| Language            | Java 21                           |
| Framework           | Spring Boot 3.5.x                 |
| Build Tool          | Maven                             |
| Database            | PostgreSQL                        |
| ORM                 | Spring Data JPA / Hibernate       |
| Migration           | Flyway                            |
| Documentation       | OpenAPI 3 / Swagger               |
| Validation          | Jakarta Validation                |
| Security            | Spring Security + JWT (Stateless) |
| Testing             | JUnit 5, Mockito, Testcontainers  |
| Metrics             | Micrometer                        |
| Monitoring          | Prometheus                        |
| Visualization       | Grafana                           |
| Distributed Tracing | OpenTelemetry + Tempo             |
| Logging             | Loki + Promtail                   |
| Health Monitoring   | Spring Boot Actuator              |
| Deployment          | Docker Compose                    |

---

# Design Goals

This implementation was designed with the following goals:

* Maintainable codebase
* Simple but expressive domain model
* Separation of concerns
* High cohesion
* Low coupling
* Production-ready observability
* Efficient database access
* Good API design
* Strong validation
* Comprehensive testing

---
