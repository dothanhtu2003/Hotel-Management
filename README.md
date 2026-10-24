# Hotel Reservation & Guest Service Management Platform

Java 21 / Spring Boot 4.1.1 / Maven. JUnit 6 đã được chấp nhận ngày 15/10/2026.

**Hiện trạng: mới có khung backend và phần M2 độc lập; backend nghiệp vụ chưa hoàn tất.**
Chưa có Controller/Service nghiệp vụ hoặc Entity/Repository/security/Redis của M3.

- [Hướng dẫn chạy test và báo cáo giới hạn](docs/m2-handoff.md)
- [Checklist ưu tiên gửi M3 để hoàn thành luồng đặt phòng](docs/m2-m3-dependencies.md)
- [OpenAPI mục tiêu](docs/api-contract.yaml)
- [Ma trận quyền](docs/m2-permission-matrix.md)
- [Quy tắc chuyển trạng thái](docs/m2-state-rules.md)
- [Quyết định cần nhóm chốt](docs/m2-decisions.md)

OpenAPI/test độc lập không phải bằng chứng MySQL/Redis, khóa, rollback, session hoặc CSRF đã hoạt động.
