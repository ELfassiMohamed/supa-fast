# Kubernetes (K8s) Microservices  Project

An asynchronous, event-driven microservices platform designed to manage patient healthcare records, provider workflows, and medical certificates. Built with Spring Boot, the architecture ensures decoupled communication, scalability, and resilience.

This project is structured for eventual deployment on a Kubernetes (K8s) environment, making use of modern cloud-native principles.

## Architecture & Tech Stack

*   **Framework**: Spring Boot 3.2.4 (Java 17/21)
*   **Datastore**: MongoDB
*   **Message Broker**: RabbitMQ
*   **Security**: Spring Security (OAuth2 JWT Resource Server)
*   **Containerization & Orchestration**: Docker & Kubernetes (Planned)

## Microservices Overview

The platform is composed of four primary microservices, each handling a distinct domain:

1.  **Patient-Service (Port 8081)**
    *   **Purpose**: Manages patient registration, authentication (acts as a JWT issuer), and profile updates.
    *   **Features**: Exposes REST endpoints for patients to manage their profiles and securely retrieves their medical history from the Medicalrecord-Service.

2.  **Provider-Service (Port 8082)**
    *   **Purpose**: Manages healthcare provider (doctors/medical staff) registration, authentication, and directory profiles.
    *   **Features**: Consumes patient registration events via RabbitMQ to keep a synchronized directory of patients under provider care.

3.  **Medicalrecord-Service (Port 8083)**
    *   **Purpose**: Serves as the central repository for patient medical records and visit history.
    *   **Features**: Secures access so that only authorized patients and their assigned providers can read or append records. Validates JWTs issued by the authentication services.

4.  **Request-Service (Port 8084)**
    *   **Purpose**: Manages medical requests (e.g., appointments, consultations) and the generation of medical certificates.
    *   **Features**: Leverages `iText` for dynamic PDF generation of medical certificates. Handles asynchronous request messaging between patients and providers.

## Event-Driven Communication

To ensure loose coupling, services communicate asynchronously via **RabbitMQ**:
*   **Patient Sync**: When a new patient registers, `Patient-Service` publishes an event. `Provider-Service` listens to this event to sync the patient directory.
*   **Requests & Responses**: Requests created by patients are dispatched to `Request-Service`, which processes them and notifies the respective provider.

## Upcoming Roadmap

*   **Phase 1: Containerization**: Dockerizing each microservice with optimized multi-stage Dockerfiles.
*   **Phase 2: Local Kubernetes**: Developing K8s manifests (Deployments, Services, ConfigMaps, Secrets) to run the cluster locally via Minikube/Docker Desktop.
*   **Phase 3: Azure Cloud Migration**: Pushing images to Azure Container Registry (ACR) and deploying the architecture to Azure Kubernetes Service (AKS).
