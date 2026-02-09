# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-02-08
- Initial release of the PIX payment service.
- REST APIs for Pix creation and status querying.
- Fraud check integration via Feign client with retry and error handling.
- RabbitMQ integration for request/response, DLQ/DLX support.
- MongoDB persistence for Pix transactions and communications.
- API key header validation for controllers.
- OpenAPI/Swagger and Postman collection included in docs.
- Unit tests with JUnit 5 and Mockito.
