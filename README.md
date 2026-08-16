# 🚀 Food Delivery System

A production-inspired event-driven microservices application that simulates the complete lifecycle of an online food delivery platform—from order placement and payment processing to restaurant preparation, delivery assignment, and real-time order tracking.

The goal of this project is to implement real-world backend concepts such as distributed communication, payment processing, reliable event publishing, schedulers, and real-time order tracking.

# 🛠 Tech Stack

Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Scheduler
- WebSocket
- RestTemplate
- REST APIs

Messaging

- Apache Kafka

Payment Gateway

- Razorpay

Database

- H2 Database

Authentication

- JWT

Build Tool

- Maven
---

## 📦 Services Created

- Auth Service
- Cart Service
- Delivery Service
- Order Service
- Restaurant Service
- Payment Service (Razorpay + Outbox + Retry + Scheduler)
- Tracking Service

  The services are currently being developed independently and will be expanded further as the project grows.
---

## ✅ Features Implemented

### 🔐 Authentication (Auth Service)
- JWT token generation
- Spring Security setup
- Basic authentication and authorization flow


### 🛒 Cart Management (Cart Service)
- Add items to cart
- Update item quantity
- Remove items from cart (in progress)
- Cart total calculation
- Validation and custom exception handling
- Restaurant-based cart restriction (single restaurant logic)

### 🍽️ Restaurant Service
- Fetch restaurant details
- Menu item retrieval
- Integration with Order Service for order validation

### 📦 Order Management (Order Service)
- Order creation and management
- Order status tracking (Created, Confirmed, Ready for Pickup, etc.)
- Integration with Cart and Restaurant services
- Event publishing for order lifecycle updates

### 🚚 Delivery Service
- Delivery assignment logic
- Delivery status updates (Assigned, Picked Up, Out for Delivery, Delivered)
- Retry mechanism for delivery partner assignment

#### ✅ Scheduler-Based Automation
Implemented background schedulers for automated delivery handling:
```text
scheduler/
├── DeliveryAssignmentScheduler
├── DeliveryMonitoringScheduler
├── MonitorAssignedTimeouts
├── MonitorSearchingTimeouts
```
- Scheduler-based automated workflows for delivery management:
    - Retry assigning delivery partners for pending orders
    - Monitor offline delivery partners and reassign deliveries
    - Handle assignment timeout (if partner does not pick up order)
    - Handle searching timeout (no partner found within time limit)
- Exception handling and validation for delivery operations
- Kafka event publishing for delivery status updates

### 💳 Payment Service

- Creates pending payments when an order is placed.
- Integrates with Razorpay Checkout.
- Verifies payment signatures after checkout.
- Validates Razorpay webhook signatures.
- Handles payment success and failure.
- Supports configurable retry attempts.
- Automatically expires inactive payments.
- Publishes payment events using the Transactional Outbox Pattern.

### 📍 Tracking Service
- Real-time delivery location updates
- WebSocket integration for live tracking
- Location update APIs
- Event-driven tracking updates

### 🔄 Kafka Event-Driven Communication
- Kafka setup using KRaft mode
- Producer implementation for events:
    - order-created
    - order-confirmed
    - order-ready-for-pickup
    - delivery-assigned
    - delivery-status-updated
    - payment-success
    - payment-failed
    - payment-expired
    - restaurant-order-status
- Consumer implementation across services
- Loose coupling between microservices using events
- Transactional Outbox Pattern for reliable Kafka publishing(currently in payment service)

### 🔗 Inter-Service Communication
- REST-based communication using RestTemplate
- Service-to-service communication between:
    - Order ↔ Restaurant
    - Order ↔ Delivery
- Decoupled service architecture design

### 🏗️ Backend Architecture
- Microservices-based architecture
- Layered design (Controller, Service, Repository)
- DTO-based request/response handling
- Custom exception handling with global handler
- Clean separation of responsibilities across services
- Transactional Outbox Pattern
- Scheduler-driven background processing
- Event-driven consistency using Kafka
---

## 🚀 System Design Highlights

- Event-driven architecture using Kafka for loose coupling
- Asynchronous communication between Restaurant, Order, Payment, Delivery and Tracking services
- Scheduler-based background processing for reliability and failure handling
- Real-time tracking using WebSocket communication
- Microservices independently deployable and scalable
- Transactional Outbox Pattern for reliable event publishing
- Secure Razorpay webhook processing
- Automatic payment expiry handling
- Retryable payment workflow

---

## 🔄 High-Level Workflow

1. User adds items to the cart and places an order *(cart-to-order flow currently in progress)*
2. Order Service creates the order
3. Order Service publishes an **"order-created"** event to Kafka
4. Payment Service consumes the event and creates a pending payment.
5. Customer completes payment using Razorpay Checkout.
6. Razorpay sends a secure webhook.
7. Payment Service validates the webhook signature and updates the payment status.
8. Payment Service stores the event using the Transactional Outbox Pattern.
9. Outbox Scheduler publishes the payment event to Kafka.
10. Order Service consumes the payment event and updates the order status.
11. Restaurant Service consumes the **"order-confirmed"** event and begins order preparation
12. Restaurant Service publishes an **"order-ready-for-pickup"** event to Kafka
13. Delivery Service consumes the event and initiates delivery partner assignment
14. Scheduler continuously monitors delivery assignment and retries if required
15. Tracking Service provides real-time delivery location updates using WebSocket *(in progress)*

#### ✅ Kafka is used to **synchronize and propagate order status updates across Order, Restaurant, Payment and Delivery services**, ensuring consistency and loose coupling between microservices.

---

## ⭐ Implemented Backend Patterns

- Transactional Outbox Pattern
- Secure Razorpay Webhook Validation
- Event-Driven Microservices
- Payment Retry Mechanism
- Automatic Payment Expiry Scheduler
- Global Exception Handling
- Kafka-based Asynchronous Communication
- Scheduler-Based Background Jobs
- WebSocket-Based Real-Time Tracking

---

## 🧩 High-Level Architecture


```text
               ┌───────────────┐
               │     User      │
               └──────┬────────┘
                      │
                      ▼
               ┌───────────────┐
               │  API Requests │
               └──────┬────────┘
                      │
                      ▼
     ┌─────────────────────────────────────┐
     │           Microservices             │
     └─────────────────────────────────────┘
     │  Auth Service       │ Cart Service  │
     │  Order Service      │ Restaurant    │
     │  Payment Service    │ Delivery      │
     │  Tracking Service                   │
     └─────────────────────────────────────┘
                       │
                       ▼
             ┌────────────────────┐
             │    Apache Kafka    │
             └────────────────────┘
                       │
                       ▼
        ┌───────────────────────────────┐
        │   Scheduler (Background Jobs) │
        └───────────────────────────────┘
                       │
                       ▼
        ┌───────────────────────────────┐
        │   Tracking Service (WS)       │
        └───────────────────────────────┘
```
---
## 🔄 Order to Delivery Flow

```text
User
  │
  ▼
Cart Service
  │
  ▼
Order Service
  │
  ├──► (order-created event) ─────► Kafka
  │
  ▼
Payment Service
  │
  ├──► (payment-success / payment-failed / payment-expired) ────────► Kafka
  │
  ▼
  Scheduler
  │
  ├── outbox event publishing
  ├── payment expiry handling
  │
  ▼
  Order Service
  │
  ├──► (order-confirmed event) ─────► Kafka
  │
  ▼
Restaurant Service
  │
  ├──► (order-ready-for-pickup) ► Kafka
  │
  ▼
Delivery Service
  │
  ├── Assign Delivery Partner
  │
  ▼
Scheduler
  │
  ├── Retry Assignment
  ├── Timeout Handling
  │
  ▼
Tracking Service
  │
  ▼
WebSocket → Live Location Updates
```
---

## ⏱️ Delivery Scheduler Workflow

```text
                         Scheduler Engine
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼

DeliveryAssignment      DeliveryMonitoring     Timeout Handlers
   Scheduler                Scheduler
(retry logic)         (partner monitoring)

        │                      │
        ▼                      ▼

  Retry Partner        Check Partner Status
   Assignment          (Active / Inactive)
        │                      │
        ▼                      ▼
  Assign Partner    ┌───────────────────────────────┐
  (if available)    │   Partner becomes inactive?   │
        │           └───────────────┬───────────────┘
        ▼                           │
     SUCCESS                    YES │ NO
        │                           ▼
        ▼                 ┌────────────────────┐
       DONE               │ Delivery Status?   │
                          └────────┬──────────┘
                                   │
                    ┌──────────────┼───────────────┐
                    │                              │
                    ▼                              ▼
             ASSIGNED STATUS             PICKED_UP / OUT_FOR_DELIVERY
                    │                              │
                    ▼                              ▼
            Reassign Partner             Mark DELIVERY_EXCEPTION

──────────────────────────────────────────────────────────────────────────

             ⏳ Timeout Handling (Separate Schedulers)

                  ┌───────────────────────────────┐
                  │    MonitorAssignedTimeouts    │
                  └───────────────┬───────────────┘
                                  │
                                  ▼
                 Assigned but not picked up in time?
                                  │
                     ┌────────────┴────────────┐
                     │                         │
                     NO                       YES
                     │                         ▼
                     │              Make Partner Available
                     │              Remove Assignment
                     │              Set SEARCHING_FOR_PARTNER
                     │              Retry Assignment
                     ▼
                    END


                 ┌───────────────────────────────┐
                 │    MonitorSearchingTimeouts   │
                 └──────────────┬────────────────┘
                                │
                                ▼
                Searching too long without partner?
                                │
                   ┌────────────┴────────────┐
                   │                         │
                   NO                       YES
                   │                         ▼
                   │              Mark DELIVERY_EXCEPTION
                   ▼
                  END

```

## 💳 Paymet Workflow

```text
Customer places order
↓
Order Service publishes order-created event to Kafka
↓
Payment Service creates pending payment
↓
Razorpay Order generated
↓
Checkout opens
↓
Webhook received
↓
Signature validated
↓
Payment updated
↓
Outbox Event
↓
Kafka
↓
Order updated
```

## ⏰ Payment Expiry Scheduler Workflow

```text
Customer places an order
        │
        ▼
Payment Service creates a payment
(Status = PENDING_PAYMENT)
        │
        ▼
Customer does not complete the payment
within the configured timeout (1 minute)
        │
        ▼
Payment Expiry Scheduler runs periodically
(every 10 seconds)
        │
        ▼
Fetch all payments with:
• Status = PENDING_PAYMENT
• updatedAt < Current Time - 1 minute
        │
        ▼
Mark payment as EXPIRED
        │
        ▼
Set failure reason:
"Payment expired due to inactivity"
        │
        ▼
Save updated payment
        │
        ▼
Create PaymentExpiredEvent
        │
        ▼
Store event in Outbox table
(Transactional Outbox Pattern)
        │
        ▼
Outbox Scheduler publishes event to Kafka
        │
        ▼
Order Service consumes
PAYMENT_EXPIRED event
        │
        ▼
Order status updated to FAILED
```

## 📤 Transactional Outbox Pattern Workflow

```text
Business Operation Occurs
(e.g., Payment SUCCESS / FAILED / EXPIRED)
        │
        ▼
Update Payment Status
in Payment table
        │
        ▼
Create corresponding event
(PaymentSuccessEvent /
 PaymentFailureEvent /
 PaymentExpiredEvent)
        │
        ▼
Save event in Outbox table
within the same database transaction
        │
        ▼
Business transaction commits
        │
        ▼
Outbox Scheduler runs periodically
        │
        ▼
Fetch all PENDING events
from Outbox table
        │
        ▼
Publish event to Kafka
        │
        ▼
Publishing Successful?
      ┌───────────────┐
      │               │
     Yes             No
      │               │
      ▼               ▼
Mark event as      Keep event
SENT               as PENDING
      │               │
      ▼               │
Other services        │
consume event         │
                      │
            Scheduler retries
            in the next execution
```
---
## 🏛 Design Patterns Used

- Transactional Outbox Pattern
- Event-Driven Architecture
- Scheduler Pattern
- Producer–Consumer Pattern
- Layered Architecture
- DTO Pattern
- Global Exception Handling
---
## Current Project Status

- Backend microservices architecture with core business workflows implemented
- Kafka-based event-driven communication between services
- Complete payment workflow with Razorpay integration
- Secure webhook signature validation for payment events
- Payment retry mechanism with configurable retry limits
- Automatic payment expiry using scheduled jobs
- Transactional Outbox Pattern for reliable Kafka event publishing
- Order, Restaurant, Delivery, and Tracking workflows integrated
- Delivery workflow and scheduler logic implemented
- WebSocket-based real-time order tracking implemented
- Focused on implementing production-grade backend patterns and system reliability
---
## Database
Currently using:
- H2 Database for development and testing
  Planned:
- Migration to MySQL in future stages of the project
---
## Kafka Setup
Kafka is configured and running in:
- KRaft Mode (without Zookeeper)
---
## Project Structure
```text
order-tracking-app/
│
├──backend/
│   ├── auth-service/
│   ├── cart-service/
│   ├── order-service/
│   ├── delivery-service/
│   ├── restaurant-service/
│   ├── payment-service/
│   └── tracking-service/
└──frontend/ (planned for future development)
```
---
## ⚡ Challenges & Solutions

- Ensuring reliable event delivery → implemented the Transactional Outbox Pattern
- Handling duplicate and fake payment callbacks → implemented Razorpay webhook signature validation
- Managing payment failures → implemented payment retry with configurable retry limits
- Handling abandoned payments → implemented scheduler-based automatic payment expiry
- Maintaining consistency across microservices → implemented Kafka-based event-driven communication
- Avoiding tight coupling between services → asynchronous communication using Kafka events
- Handling delivery partner unavailability → solved using scheduler-based retry logic
- Handling real-time delivery updates → implemented WebSocket-based tracking service
---
## 💡 Key Highlights

- Event-driven microservices architecture using Apache Kafka
- Secure Razorpay payment integration with webhook signature validation
- Transactional Outbox Pattern for reliable event publishing
- Payment retry and automatic payment expiry mechanisms
- Scheduler-based background processing
- Real-time order tracking using WebSockets
- Modular microservices designed for scalability and loose coupling
---
## Learning Goals Behind This Project
This project is mainly being built to understand:
- Microservices architecture
- Backend system design
- Event-driven communication using Kafka
- Secure authentication systems
- Scalable backend development practices
- Real-world API development
- Frontend design and integration using modern frameworks
---
## How to Run

### Prerequisites
- Java 17+
- Maven( Intellij IDE have Maven pre-installed)
- Apache Kafka
- Razorpay account for payment integration
- ngrok (for exposing local webhook endpoint to Razorpay)
- Postman (for API testing)

--------------------------------------------------

### ✅ Steps

1. Clone the repository

--------------------------------------------------

2. Start Kafka Server (KRaft Mode)

→ Create Kafka Cluster (only once)

bin\windows\kafka-storage.bat random-uuid

Copy the generated UUID


--------------------------------------------------

→ Format Kafka Storage (only once)

bin\windows\kafka-storage.bat format --standalone -t <your-UUID> -c config\server.properties

Replace <your-UUID> with the generated UUID

--------------------------------------------------

→ Start Kafka Server

bin\windows\kafka-server-start.bat config\server.properties

--------------------------------------------------

3. Create Kafka Topics

Option 1: Create individually

bin\windows\kafka-topics.bat --create --topic <topic-name> --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

--------------------------------------------------

Option 2 (Recommended): Use batch file

Create file: create-topics.bat

Paste:

@echo off
echo Creating Kafka topics...

call bin\windows\kafka-topics.bat --create --topic delivery-status-updated --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
call bin\windows\kafka-topics.bat --create --topic delivery-assigned --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
call bin\windows\kafka-topics.bat --create --topic order-confirmed --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
call bin\windows\kafka-topics.bat --create --topic order-created --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
call bin\windows\kafka-topics.bat --create --topic order-ready-for-pickup --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
call bin\windows\kafka-topics.bat --create --topic payment-success --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
call bin\windows\kafka-topics.bat --create --topic restaurant-order-status --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
call bin\windows\kafka-topics.bat --create --topic payment-failed --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
call bin\windows\kafka-topics.bat --create --topic payment-expired --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

echo All topics created successfully!
pause


Run it:

create-topics.bat

--------------------------------------------------

→ Verify topics

bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

--------------------------------------------------

Required Topics:

- delivery-status-updated
- delivery-assigned
- order-confirmed
- order-created
- order-ready-for-pickup
- payment-success
- payment-failed
- payment-expired
- restaurant-order-status

--------------------------------------------------

4. Start Microservices (in order)

- Auth Service
- Restaurant Service
- Order Service
- Payment Service
- Delivery Service
- Tracking Service
- Cart Service

--------------------------------------------------

5. Test APIs

Use Postman to test REST APIs

--------------------------------------------------

6. Optional

Use browser or WebSocket client for real-time tracking updates

---
## Future Enhancements
- Idempotency Keys for Payment Creation
- Generic `payment-events` Kafka Topic
- Outbox Retry Mechanism with Retry Count
- MySQL Migration
- Service Discovery (Eureka)
- Spring Cloud Config Server
- Dead Letter Queue (DLQ)
- Optimistic Locking
- Refund Workflow
- Metrics & Monitoring
- Correlation IDs
- Admin Dashboard
- API Gateway implementation
- Frontend development using React or Angular
- Docker and containerization
- Redis caching
- Notification service
- Real-time order tracking
- CI/CD pipeline setup

---
## Status
🚀 This project is actively evolving with continuous improvements, feature additions, and production-level enhancements.

---
# 👩‍💻 Author

**Meghana K**

FullStack Developer | Java | Spring Boot | Microservices | Kafka
