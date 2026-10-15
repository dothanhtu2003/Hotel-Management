# Quyết định cần nhóm chốt

Nguồn đối chiếu: V1 mục 5, 7–12, 14. Không tự thêm mã lỗi, đổi URL hoặc schema trong lúc chờ.
OpenAPI là hợp đồng mục tiêu; các điểm dưới được ghi rõ thay vì coi đề xuất là yêu cầu đã duyệt.

## Đã chốt với người dùng

- Java 21, giữ Spring Boot 4.1.1 và chấp nhận JUnit 6 (15/10/2026); không đổi framework/project.
- Controller → Service → Repository, DTO tại biên API; M3 sở hữu dữ liệu/auth/security/Redis.
- Sort id tăng cho tất cả danh sách, page 0, size 20/max 100 theo mặc định V1.
- VND nguyên; giờ nghiệp vụ Asia/Ho_Chi_Minh; token không được log.

## Cần M1/M2/M3/M4 xác nhận trước tích hợp

| ID | Quyết định | Đề xuất / hiện trạng | Ảnh hưởng |
|---|---|---|---|
| D01 | Mã lỗi framework 404/405/415/406 và lỗi validation tham số | V1 không định nghĩa mã riêng. Hiện handler giữ HTTP status, dùng INTERNAL_ERROR cho framework chưa phân loại; JSON/kiểu/thiếu tham số dùng VALIDATION_ERROR. Cần chốt dùng mã hiện có hay bổ sung mã chuyên biệt. | ErrorCode, frontend, OpenAPI, test MVC |
| D02 | 404 cho Invoice/ServiceOrder thiếu hoặc sai chủ | V1 có BOOKING_NOT_FOUND nhưng không có INVOICE_NOT_FOUND/SERVICE_ORDER_NOT_FOUND. Tài liệu cũ đã tự coi BOOKING_NOT_FOUND là mã chung; giả định đó được rút lại. OpenAPI chỉ cam kết 404 + ApiError, chưa áp mã tài nguyên cụ thể. | Không tiết lộ tồn tại của tài nguyên; không tự thêm mã trước review |
| D03 | HTTP của từng mã nghiệp vụ chưa ghi rõ trong V1 | 400: ngày sai/trước hôm nay/capacity/item trùng; 409: state/room/service conflict; 404: ROOM/GUEST/BOOKING_NOT_FOUND. BILL_CHANGED, HOLD_EXPIRED, INVALID_HOLD, ROOM_ON_HOLD = 409 và Redis acquire/verify lỗi = 503 đã rõ trong V1. Review bảng ErrorCode hiện có. | Frontend và Service tests |
| D04 | MySQL/Redis image cho dev và integration | Testcontainers hiện dùng mysql:latest và redis:latest. Nhóm chốt exact patch tag hoặc digest, cùng major/patch với môi trường demo; không tự chọn phiên bản sản phẩm thay M3/M4. Sau chốt pin fixture/Compose/CI và ghi nguồn cấu hình. | Tái hiện schema/khóa/Lua/TTL |
| D05 | Lock timeout/deadlock translation | Hiện chỉ CannotAcquireLockException, JPA LockTimeoutException/PessimisticLockException → 409 RESOURCE_BUSY. Generic concurrency, optimistic locking, transaction serialization → 500 INTERNAL_ERROR. M3/M4 xác nhận Hibernate/MySQL thực tế, kể cả exception bị bọc lúc flush/commit; chỉ thêm mapping khi có bằng chứng lỗi khóa. | Không che lỗi chương trình hoặc optimistic concurrency thành lỗi bận |
| D06 | Principal chính thức và session/CSRF | Dùng principal của M3; xác nhận tên type/getter userId/role/guestId. JSESSIONID và X-CSRF-TOKEN là tên mặc định đề xuất trong OpenAPI; runtime lấy headerName từ /auth/csrf. | Tránh tự giải mã session hay tin ID client |
| D07 | Release token lookup và Redis lỗi lúc release | Chốt locator + secret hoặc token index cùng TTL; release ownership/compare-delete nguyên tử. 204 hết hạn/đã xóa, 403 khác chủ đã rõ. 503 khi Redis lỗi lúc release là đề xuất vì V1 chỉ quy định rõ acquire/createBooking. | Contract Redis và API release |
| D08 | Biên ngày check-out và preview CHECKED_OUT | Đề xuất checkInDate <= hôm nay <= checkOutDate; khoảng chặn lịch vẫn [in,out). Ngày trả được cho gọi dịch vụ nếu còn CHECKED_IN theo V1. Đề xuất preview CHECKED_OUT trả 409 INVALID_STATUS_TRANSITION, client gọi invoice API; không đổi BillPreviewResponse thành InvoiceResponse. | V1 viết “trong khoảng ngày lưu trú” và “xem hóa đơn đã lưu” nhưng chưa chỉ rõ HTTP của preview đã kết thúc |
| D09 | Tiền request và trường lạ | OpenAPI target chỉ nhận chuỗi số nguyên, additionalProperties=false. BigDecimal mapper hiện có vẫn có thể nhận JSON number; mapper hiện tại chưa bật reject unknown fields. Chốt strict ở request nghiệp vụ, không sửa mapper/security M3 tùy ý. guestId:null vẫn bị cấm với GUEST. | Không coi schema target đã được enforce; test request đầy đủ sau tích hợp |
| D10 | roomType, ID, code và giới hạn tiền | roomType đang String vì chưa có enum; ID JSON phải <= 2^53−1. Chốt format/uniqueness bookingCode/invoiceNumber và xử lý overflow DECIMAL(18,0) trước save, không nhận giá từ frontend. | Schema và factory Entity M3 |

Không có câu trả lời của nhóm được suy ra từ việc tài liệu/test đã được tạo.
M2 vẫn làm tài liệu, DTO/policy và test độc lập; các quyết định này chặn phần Service tương ứng khi tích hợp.
