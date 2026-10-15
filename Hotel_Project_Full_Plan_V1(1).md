# HOTEL RESERVATION & GUEST SERVICE MANAGEMENT PLATFORM

## Bản phân công và đặc tả triển khai V1 — cập nhật

Ngày: 15/10/2026. Ngôn ngữ tài liệu: tiếng Việt. Tên trong code: tiếng Anh.

Ghi nhận rà soát 15/10/2026: người dùng đã chốt giữ Spring Boot 4.1.1 và chấp nhận JUnit 6 của project hiện tại thay JUnit 5. Các điểm còn cần nhóm chốt được tách tại docs/m2-decisions.md; OpenAPI là hợp đồng mục tiêu, không phải bằng chứng API đã triển khai.

Cơ sở công nghệ: Week 8 – Clean Code & Coding Conventions Plan của nhóm. Các chính sách nghiệp vụ và lựa chọn HTTP session dưới đây là phương án triển khai đề xuất cho dự án môn học; nhóm cần thống nhất trước khi code. Redis dùng giữ phòng tạm, không thay thế transaction và khóa MySQL.

## 1. Mục tiêu và phạm vi

Xây dựng web quản lý đặt phòng và dịch vụ khách sạn với 5 nhóm chức năng:

1. Tìm phòng trống theo ngày nhận, ngày trả, số khách.
2. Đặt một phòng cho nhiều đêm, không đặt trùng lịch.
3. Gọi và xử lý dịch vụ trong thời gian lưu trú.
4. Theo dõi, cập nhật tình trạng dọn phòng.
5. Tổng hợp tiền phòng và dịch vụ, thu tiền và trả phòng.

Hệ thống có đăng ký tài khoản khách, đăng nhập, hồ sơ khách, đặt phòng trực tuyến và lễ tân đặt hộ khách vãng lai. Chỉ quản lý một khách sạn, đơn vị tiền VND, thanh toán toàn bộ bằng tiền mặt tại quầy.

**Ngoài phạm vi phiên bản đầu:** Thanh toán trực tuyến, đặt cọc/hoàn tiền, khuyến mãi ; đặt nhiều phòng trong một lượt; quản lý nhiều khách sạn; đổi phòng, đổi ngày và gia hạn; quản lý tồn kho dịch vụ; giao diện quản trị phòng, dịch vụ và tài khoản.

**Dữ liệu ban đầu:** Danh sách phòng, dịch vụ và tài khoản nhân viên được tạo bằng migration/seed.


## 2. Công nghệ và cấu trúc chung

| Phần | Lựa chọn đề xuất |
|---|---|
| Frontend | TypeScript + React, Vite, Fetch API, CSS |
| Backend | Java 21, Spring Boot, Maven |
| Truy cập dữ liệu | Spring Data JPA/Hibernate |
| Database | MySQL; cùng loại database cho integration test |
| Cache / giữ phòng tạm | Redis, TTL 15 phút theo kế hoạch tuần 8 |
| CI/CD | GitHub Actions: build, test và kiểm tra chất lượng khi push/PR |
| Migration | Flyway; không để Hibernate tự sửa schema |
| Bảo mật | Spring Security, HTTP session, cookie và CSRF |
| Test | JUnit 6 (đã chốt 15/10/2026), Mockito, Spring Boot Test, Testcontainers |
| Quản lý mã nguồn | Một GitHub repository, pull request trước khi merge |



Cấu trúc repository:

- frontend/: ứng dụng React; src/components, pages, api, hooks, utils.
- backend/: ứng dụng Spring Boot.
- docs/: đặc tả, sơ đồ, API, test plan, báo cáo.
- backend/src/main/resources/db/migration/: migration Flyway.

Package gốc backend: com.hotelmanagement. Các package con: controller, service, policy, repository, entity, dto, exception, security, config. M2 quản lý controller/service nghiệp vụ, dto nghiệp vụ, policy và exception. M3 quản lý entity/repository, security, cấu hình database và các lớp auth/profile và RedisHoldService. Không duy trì đồng thời một lớp Booking domain và BookingEntity trong V1: dùng BookingEntity nội bộ, DTO ở biên API.

## 3. Phân công thành viên

| Thành viên | Trách nhiệm chính | Đầu ra |
|---|---|---|
| M1 – Phommasone Panadda – 52400346 | Toàn bộ frontend: khách, lễ tân, housekeeping; tích hợp API và cookie/CSRF | Giao diện hoạt động, component, module API, wireframe, sơ đồ luồng client |
| M2 – Đỗ Thanh Tú – 52200240 | Backend nghiệp vụ: phòng trống, booking, nhận phòng, đơn dịch vụ, housekeeping, hóa đơn, trả phòng | Controller, Service, DTO, policy, xử lý lỗi, hợp đồng API và sơ đồ backend |
| M3 – Shinn Thant – 52400347 | Schema, migration, seed, Entity, Repository, truy vấn/khóa, Redis giữ phòng tạm, đăng nhập và bảo mật, hồ sơ khách | ERD, mã dữ liệu và auth/profile, hướng dẫn cấu hình, kiểm thử phần mình |
| M4 – Su Nay Yee Nwe – 52400345 | Test plan, unit/integration/API/UI test, báo lỗi, tổng hợp sơ đồ và báo cáo | Test tự động, workflow GitHub Actions, traceability matrix, báo cáo kết quả, checklist Clean Code |

Ranh giới cụ thể:

- M3 viết Entity và Repository; M2 thống nhất chữ ký phương thức với M3 rồi gọi từ Service.
- M3 sở hữu AuthController/AuthService, GuestController/GuestService và phần security. M2 sở hữu các API nghiệp vụ còn lại.
- M3 viết RedisHoldService, thao tác Redis nguyên tử, query và phương thức khóa; M2 sở hữu transaction nghiệp vụ và API giữ phòng; M4 xác minh cả nghiệp vụ lẫn truy vấn và concurrency.
- M1 không quyết định tổng tiền, quyền truy cập hoặc tính hợp lệ của chuyển trạng thái.
- M4 không phải người duy nhất kiểm tra code. Người viết phải tự chạy kiểm tra liên quan trước bàn giao; M4 phụ trách bộ kiểm thử chung.
- M2 và M3 cùng review mọi thay đổi ảnh hưởng schema/API nhưng chỉ có một người sửa mỗi file tại một thời điểm.

## 4. Tên gọi thống nhất

| Khái niệm | Entity | Repository | Controller / Service |
|---|---|---|---|
| Tài khoản | UserEntity | UserRepository | AuthController / AuthService |
| Hồ sơ khách | GuestEntity | GuestRepository | GuestController / GuestService |
| Phòng | RoomEntity | RoomRepository | RoomController / RoomService |
| Giữ phòng tạm | Không có bảng MySQL | RedisHoldService do M3 viết | RoomHoldController / RoomHoldService do M2 viết |
| Đặt phòng | BookingEntity | BookingRepository | BookingController / BookingService |
| Danh mục dịch vụ | HotelServiceEntity | HotelServiceRepository | ServiceCatalogController / ServiceCatalogService |
| Đơn dịch vụ | ServiceOrderEntity | ServiceOrderRepository | ServiceOrderController / ServiceOrderService |
| Dòng đơn dịch vụ | ServiceOrderItemEntity | Theo aggregate ServiceOrder | Không có API riêng |
| Dọn phòng | Dùng RoomEntity | Dùng RoomRepository | HousekeepingController / HousekeepingService |
| Hóa đơn | InvoiceEntity | InvoiceRepository | InvoiceController / BillingService |
| Dòng hóa đơn | InvoiceItemEntity | Theo aggregate Invoice | Không có API riêng |

Dùng BookingService thay ReservationService trong kế hoạch tuần 8; ghi ánh xạ tên này trong báo cáo để thống nhất tài liệu. RoomService quản lý phòng và tìm phòng; ServiceOrderService xử lý dịch vụ khách gọi. Policy: RoomAvailabilityPolicy. Không dùng tên Service đơn lẻ cho entity dịch vụ.

Frontend: RoomSearchForm, RoomList, RoomCard, BookingForm, BookingConfirmation, MyBookingsPage, MyInvoicesPage, FrontDeskDashboard, BookingTable, BookingDetails, ServiceCatalog, ServiceOrderForm, ServiceOrderList, HousekeepingBoard, HousekeepingRoomCard, InvoiceDetails, CheckoutForm, LoginForm, RegisterForm, GuestProfileForm.

Module gọi API: authApi, guestApi, roomApi, roomHoldApi, bookingApi, serviceCatalogApi, serviceOrderApi, housekeepingApi, invoiceApi.

## 5. Quy tắc đặt tên và dữ liệu

| Nội dung | Quy ước |
|---|---|
| Lớp Java / component React | PascalCase |
| Biến, phương thức, JSON | camelCase |
| Bảng, cột database | snake_case; bảng số nhiều |
| Type / Interface TypeScript | PascalCase: Booking, RoomType |
| Hook / Store frontend | useBooking, useRoomSearch; bookingStore, authStore |
| Test Java | BookingServiceTest; testCreateBooking(), testOverbooking() |
| Package Java | Chữ thường |
| Index / constraint SQL | idx_table_column; chk_table_rule |
| Enum / hằng số | UPPER_SNAKE_CASE |
| Ngày lưu trú | yyyy-MM-dd; Java LocalDate |
| Thời điểm sự kiện | ISO-8601 có offset; lưu UTC |
| Ngày nghiệp vụ | Asia/Ho_Chi_Minh; inject Clock để kiểm thử |
| ID | BIGINT tự sinh; JSON number trong phạm vi số nguyên an toàn của JavaScript |
| Tiền | Java BigDecimal; MySQL DECIMAL(18,0); JSON chuỗi số nguyên, ví dụ "1200000" |
| Đồng tiền | currency = VND |
| Số khách/số lượng | Số nguyên dương |
| URL API | /api; danh từ số nhiều; không dùng tên bảng làm dữ liệu trả trực tiếp |
| DTO | CreateBookingRequest, BookingResponse, RoomResponse, CreateServiceOrderRequest, InvoiceResponse |

Các trường dùng chung: checkInDate, checkOutDate, guestCount, nightlyRate, roomAmount, serviceAmount, totalAmount. Không đổi giữa checkIn và checkInDate tùy màn hình. Mật khẩu và token CSRF không được ghi log. Không trả Entity trực tiếp qua API.

## 6. Database và quan hệ

Dùng MySQL với engine InnoDB, charset utf8mb4; chốt cùng phiên bản cho dev và test. ID dùng BIGINT AUTO_INCREMENT; tiền DECIMAL(18,0), ngày DATE, thời điểm lưu UTC; enum ánh xạ chuỗi.

Mọi bảng giao dịch có created_at; các bảng thay đổi có updated_at. Khóa ngoại phải có ràng buộc thật trong database. Không cascade xóa lịch sử giao dịch từ danh mục.

| Bảng | Các cột chính và ràng buộc |
|---|---|
| users | id PK, email UNIQUE NOT NULL (chuẩn hóa chữ thường), password_hash, role, enabled |
| guests | id PK, user_id FK users UNIQUE NULLABLE, full_name, phone, email NULLABLE |
| rooms | id PK, room_number UNIQUE, room_type, capacity > 0, nightly_rate >= 0, room_status, housekeeping_status |
| bookings | id PK, booking_code UNIQUE, guest_id FK, room_id FK, created_by_user_id FK, check_in_date, check_out_date, guest_count > 0, nightly_rate_snapshot >= 0, room_amount >= 0, booking_status, checked_in_at NULLABLE, checked_out_at NULLABLE, cancelled_at NULLABLE |
| hotel_services | id PK, name, unit_price >= 0, active |
| service_orders | id PK, booking_id FK, created_by_user_id FK, service_order_status, total_amount >= 0 |
| service_order_items | id PK, service_order_id FK, hotel_service_id FK, service_name_snapshot, quantity > 0, unit_price_snapshot >= 0, line_amount >= 0; UNIQUE(order, service) |
| invoices | id PK, invoice_number UNIQUE, booking_id FK UNIQUE, room_amount, service_amount, total_amount, currency, payment_status, payment_method, paid_at, collected_by_user_id FK, guest_name_snapshot, room_number_snapshot, check_in_date_snapshot, check_out_date_snapshot |
| invoice_items | id PK, invoice_id FK, item_type, description, quantity > 0, unit_price >= 0, line_amount >= 0 |

Quan hệ: User 0..1 Guest; Guest 1..n Booking; Room 1..n Booking; Booking 1..n ServiceOrder; ServiceOrder 1..n ServiceOrderItem; HotelService 1..n ServiceOrderItem; Booking 0..1 Invoice; Invoice 1..n InvoiceItem.

Booking phải có check_out_date > check_in_date. Guest có thể không có User cho walk-in. Tài khoản nhân viên không bắt buộc có Guest. Guest email không unique vì nhiều hồ sơ có thể dùng chung email liên hệ; User email đăng nhập unique. Không tự liên kết tài khoản mới với hồ sơ walk-in chỉ dựa trên email/điện thoại.

Index tối thiểu: bookings(room_id, booking_status, check_in_date, check_out_date), bookings(guest_id), service_orders(booking_id, service_order_status), guests(phone), invoices(booking_id) UNIQUE. Index không tự bảo đảm chống trùng lịch; quy trình khóa ở mục 9 là bắt buộc.


## 7. Trạng thái và chuyển trạng thái

| Enum | Giá trị |
|---|---|
| Role | GUEST, FRONT_DESK, HOUSEKEEPING |
| BookingStatus | CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED |
| RoomStatus | ACTIVE, OUT_OF_SERVICE |
| HousekeepingStatus | DIRTY, IN_PROGRESS, CLEAN |
| ServiceOrderStatus | ORDERED, CANCELLED |
| PaymentStatus | PAID |
| PaymentMethod | CASH |
| InvoiceItemType | ROOM, SERVICE |

V1 chưa lưu hóa đơn chưa thanh toán: bill-preview là dữ liệu tính tạm. Do đó PaymentStatus chỉ có PAID; chỉ thêm UNPAID khi có luồng phát hành hóa đơn trước thanh toán. Không có role ADMIN chưa sử dụng.

| Đối tượng | Chuyển hợp lệ | Điều kiện |
|---|---|---|
| Booking | Tạo → CONFIRMED | Đủ điều kiện và không trùng lịch |
| Booking | CONFIRMED → CANCELLED | Ngày hiện tại trước ngày nhận |
| Booking | CONFIRMED → CHECKED_IN | Đúng ngày nhận, phòng ACTIVE và CLEAN |
| Booking | CHECKED_IN → CHECKED_OUT | Trong khoảng ngày lưu trú, đã thu đủ tiền |
| ServiceOrder | Tạo → ORDERED | Booking CHECKED_IN, chưa quá ngày trả; ghi nhận để tính tiền |
| ServiceOrder | ORDERED → CANCELLED | Lễ tân hủy trước khi booking CHECKED_OUT |
| Housekeeping | DIRTY → IN_PROGRESS | Nhân viên bắt đầu dọn |
| Housekeeping | IN_PROGRESS → CLEAN | Nhân viên xác nhận hoàn tất |
| Housekeeping | CLEAN → DIRTY | Hệ thống tự chuyển khi check-out |

Chuyển ngoài bảng bị từ chối. Không hủy booking đã nhận phòng; không nhận phòng trước/ngày sau ngày nhận trong V1. Không mở lại booking đã kết thúc. Yêu cầu đặt lại đúng trạng thái hiện tại của đơn/housekeeping trả thành công nhưng không tạo tác dụng phụ.

## 8. Quy tắc nghiệp vụ

### 8.1 Tìm và đặt phòng

- Người chưa đăng nhập được tìm phòng. Khách phải đăng nhập trước khi đặt.
- checkInDate không trước ngày hiện tại; checkOutDate > checkInDate; guestCount > 0 và không vượt capacity.
- Một booking gắn một phòng và một hồ sơ khách đại diện.
- Trước khi tạo booking, khách hoặc lễ tân giữ phòng tạm và nhận holdToken; gửi token cùng dữ liệu đặt phòng. Token gắn với tài khoản, phòng, ngày và số khách.
- Đêm lưu trú theo khoảng [checkInDate, checkOutDate). Nhận 01/11, trả 03/11 tính 2 đêm: 01 và 02; booking khác được bắt đầu 03/11.
- Phòng phải ACTIVE. Phòng DIRTY vẫn có thể xuất hiện khi tìm/đặt cho lịch phù hợp, nhưng không được check-in cho đến khi CLEAN.
- CONFIRMED và CHECKED_IN giữ phòng theo khoảng ngày đặt. CANCELLED/CHECKED_OUT không giữ phòng.
- Khi search, backend lấy giá từ danh mục. Khi tạo booking, backend chốt nightlyRateSnapshot và roomAmount; không nhận giá từ frontend.
- roomAmount = số đêm × nightlyRateSnapshot. Thay đổi giá danh mục không thay đổi booking đã tạo.
- Hủy miễn phí trước ngày nhận; từ ngày nhận trở đi không được hủy bằng API V1.
- Trả sớm vẫn tính đủ số đêm đã đặt. Không hỗ trợ gia hạn hoặc trả quá ngày dự kiến.

### 8.2 Dịch vụ

- Chỉ tạo đơn khi booking CHECKED_IN và ngày hiện tại không sau checkOutDate.
- Khách chỉ gọi cho booking của mình; lễ tân có thể gọi hộ.
- Mỗi đơn có ít nhất một item; quantity > 0; không chấp nhận hotelServiceId lặp trong một request; dịch vụ phải active.
- Chốt tên/giá dịch vụ vào từng item. lineAmount = quantity × unitPriceSnapshot. totalAmount = tổng lineAmount.
- Tạo đơn ở trạng thái ORDERED; toàn bộ đơn ORDERED được cộng vào hóa đơn. CANCELLED không tính tiền.
- Không theo dõi tiến độ phục vụ trong V1; không có PLACED/IN_PROGRESS/COMPLETED và không chặn check-out vì đơn chưa hoàn thành.
- Không chỉnh sửa item sau khi tạo. Lễ tân được hủy đơn ORDERED trước khi check-out; khách có thể yêu cầu lễ tân hủy, không có API tự hủy dành cho khách.
- Sau khi CHECKED_OUT, không tạo/hủy/sửa đơn dịch vụ.

### 8.3 Trả phòng và tiền

- Chỉ FRONT_DESK được xác nhận thu tiền/check-out.
- totalAmount = roomAmount + tổng đơn ORDERED. Không thuế, phụ phí, giảm giá.
- Check-out request có expectedTotalAmount, paymentMethod = CASH, paymentConfirmed = true. Backend tự tính lại; số tiền thay đổi so với màn hình thì trả 409 BILL_CHANGED để lễ tân xem lại.
- Lễ tân chỉ gửi xác nhận sau khi đã thu đủ tiền mặt. Không tự động thu tiền qua API.
- Trong một transaction: khóa Room rồi Booking → kiểm tra → tính tiền → lưu Invoice PAID và các dòng snapshot → đổi booking CHECKED_OUT → room DIRTY.
- Thất bại ở bước nào thì rollback toàn bộ. UNIQUE invoice.booking_id và khóa booking ngăn tạo hóa đơn kép.
- Check-out lặp lại cho booking đã hoàn tất trả hóa đơn cũ; không ghi thu tiền lần hai.
- Bill preview không cam kết giá trị đến khi check-out, có canCheckOut dựa trạng thái booking và ngày hiện tại; không kiểm tra tiến độ phục vụ.

## 9. Chống đặt trùng và race condition

M3 cung cấp phương thức findByIdForUpdate(roomId) với PESSIMISTIC_WRITE trên dòng Room; query lấy booking có khả năng giao nhau theo điều kiện:

```sql
existing.check_in_date < :newCheckOut
AND existing.check_out_date > :newCheckIn
AND existing.booking_status IN ('CONFIRMED', 'CHECKED_IN')
```

M2 viết BookingService.createBooking() có @Transactional:

1. Xác định người dùng và hồ sơ Guest hợp lệ.
2. Kiểm tra định dạng/điều kiện ngày và số khách.
3. Khóa dòng Room bằng phương thức M3 cung cấp; kiểm tra holdToken còn hạn, đúng chủ sở hữu và đúng dữ liệu request trong Redis.
4. Kiểm tra ACTIVE, capacity và lấy booking giao nhau trong cùng transaction.
5. Dùng RoomAvailabilityPolicy xác nhận điều kiện, chốt giá.
6. Lưu booking CONFIRMED; commit mới giải phóng khóa. Sau commit, xóa hold bằng thao tác so token rồi xóa nguyên tử. Nếu xóa thất bại, hold tự hết TTL; không báo booking thất bại sau khi đã commit.

Không khóa booking thay cho Room vì phòng có thể chưa có booking nào. Mọi đường tạo booking đều khóa cùng dòng Room trước khi kiểm tra. Cấu hình transaction tạo booking dùng READ_COMMITTED; không bật query cache cho truy vấn xung đột. Không đặt query kiểm tra ở transaction REQUIRES_NEW riêng.

RoomAvailabilityPolicy có phương thức overlaps(existingStart, existingEnd, requestedStart, requestedEnd) và logic đánh giá phòng. M3 có thể lọc query theo khoảng ngày để giảm dữ liệu; M4 kiểm tra query và policy cùng các trường hợp biên. RoomService và BookingService cùng dùng policy; policy không thay thế khóa hoặc transaction.

Quy ước khóa cho các thao tác khác: nếu cần cả Room và Booking thì khóa Room trước, Booking sau. Mọi thay đổi đơn dịch vụ khóa Booking cha trước khi kiểm tra CHECKED_IN; check-out cũng khóa Booking trước khi đọc đơn/tính tiền. Nhờ đó việc tạo hoặc hủy đơn ORDERED không chen vào lúc chốt hóa đơn. M3 cung cấp truy vấn khóa; M2 điều phối transaction. Lỗi hết thời gian chờ khóa trả 409 RESOURCE_BUSY, không tạo dữ liệu một phần.

### 9.1 Redis giữ phòng tạm — phạm vi đơn giản cho môn học

- M3 viết RedisHoldService; M2 viết RoomHoldService/Controller và tích hợp vào BookingService. M1 hiển thị đếm ngược; M4 test TTL, quyền và xung đột.
- Mỗi Room có một key `room-hold:{roomId}`. Value chứa holdToken ngẫu nhiên, userId, checkInDate, checkOutDate, guestCount. TTL cấu hình `MAX_HOLD_MINUTES = 15`.
- V1 giữ cả phòng trong thời gian TTL, kể cả khi người khác chọn khoảng ngày khác. Đây là lựa chọn đơn giản, có thể làm giảm khả năng đặt trong 15 phút nhưng tránh phải triển khai nhiều hold theo từng đêm. Không tuyên bố đây là thiết kế tối ưu cho vận hành thật.
- Tạo hold: trong transaction MySQL, khóa Room → kiểm tra lịch booking → nếu key chưa có thì tạo bằng Redis SET NX kèm TTL. Nếu key đã có, trả 409 ROOM_ON_HOLD; không ghi đè hoặc gia hạn tự động.
- Tạo hold và tạo booking đều khóa cùng dòng Room để tuần tự hóa kiểm tra booking/hold. Redis không tham gia transaction MySQL: nếu tạo key rồi transaction giữ phòng thất bại, xóa đúng token theo best effort; TTL dọn phần còn lại.
- Tạo booking xác minh hold khi đang giữ khóa Room. Token phải còn hạn tại thời điểm kiểm tra; sau đó khóa MySQL bảo vệ đến commit. Nếu TTL hết trong lúc lưu, tác vụ tạo hold khác vẫn phải đợi khóa Room và kiểm tra lại booking sau commit.
- Hủy hold chỉ chủ sở hữu được làm; M3 dùng Lua hoặc cơ chế compare-and-delete nguyên tử. Không GET rồi DEL riêng lẻ vì có thể xóa nhầm hold mới sau khi TTL hết.
- Token hết hạn trả 409 HOLD_EXPIRED; token không khớp dữ liệu trả 409 INVALID_HOLD. Khi Redis không truy cập được, tạo hold/booking trả 503 HOLD_SERVICE_UNAVAILABLE; không bỏ qua giữ phòng để đặt tiếp.
- Search không phải cam kết phòng đang được giữ cho người tìm. M1 chỉ chuyển sang xác nhận đặt khi tạo hold thành công. V1 không cần search tải trạng thái Redis cho mọi phòng.
- Giữ phòng kết thúc khi đặt thành công, người dùng chủ động hủy hoặc TTL hết. Không triển khai gia hạn hold.
- Redis không lưu session đăng nhập trong V1; HTTP session và Redis giữ phòng là hai chức năng riêng.

## 10. HTTP session, quyền và hồ sơ

- Dùng Spring Security HTTP session, không JWT; Redis chỉ dùng giữ phòng tạm trong V1.
- Password lưu dạng hash bằng PasswordEncoder; không lưu/so sánh mật khẩu thô.
- Session timeout đề xuất 30 phút không hoạt động. Đổi session ID sau đăng nhập; logout vô hiệu hóa session và xóa cookie.
- Cookie phiên HttpOnly; Secure khi chạy HTTPS; SameSite theo cấu hình triển khai. Ưu tiên cùng origin với /api reverse proxy.
- Khi frontend/backend khác origin: M3 chỉ cho phép origin cụ thể, bật credentials; M1 dùng credentials: include. Không dùng wildcard origin với cookie.
- GET /api/auth/csrf cung cấp token; M1 gửi X-CSRF-TOKEN cho POST/PATCH. Sau login/logout phải lấy token mới.
- API trả JSON 401/403, không chuyển hướng sang trang login HTML.
- Đăng ký luôn tạo GUEST; không nhận role hoặc userId từ client. Register tạo User + Guest trong cùng transaction, không tự đăng nhập.
- guest.userId là liên kết sở hữu chính thức. Không chỉ so email, phone hoặc ID client gửi để cho quyền.
- Chưa đăng nhập: chỉ register/login/csrf, tìm/xem phòng và xem danh mục dịch vụ.
- HOUSEKEEPING chỉ xem danh sách phòng phục vụ dọn và cập nhật trạng thái cho phép; không xem thông tin khách/hóa đơn.
- FRONT_DESK xem và thao tác nghiệp vụ vận hành. Nhân viên seed không có quyền sửa cấu hình hệ thống qua API.
- Hồ sơ walk-in không tự trở thành tài khoản. Không có luồng claim hồ sơ cũ trong V1.

## 11. Hợp đồng API chung

Content-Type application/json. Success trả DTO trực tiếp; danh sách trả {items, page, size, totalElements}. page bắt đầu 0, mặc định size=20, tối đa 100. Mặc định sắp xếp id tăng; booking/invoice có thể sắp createdAt giảm nhưng phải ghi trong OpenAPI.

Mã HTTP: 200 thành công; 201 tạo mới; 204 logout hoặc release hold; 400 dữ liệu sai; 401 chưa đăng nhập; 403 sai quyền/CSRF; 404 không tìm thấy hoặc tài nguyên của khách khác; 409 xung đột nghiệp vụ; 503 dịch vụ giữ phòng tạm không khả dụng. Không trả stack trace hoặc chi tiết database.

Mẫu lỗi:

```json
{
  "code": "ROOM_NOT_AVAILABLE",
  "message": "Phòng không còn trống trong khoảng ngày đã chọn.",
  "fieldErrors": [],
  "details": {},
  "timestamp": "2026-10-15T04:00:00Z"
}
```

fieldErrors là mảng {field,message}; details có thể chứa currentTotalAmount. M1 dựa code để xử lý/i18n, không so chuỗi message.

Mã lỗi thống nhất: VALIDATION_ERROR, INVALID_DATE_RANGE, CHECK_IN_DATE_IN_PAST, ROOM_NOT_FOUND, ROOM_NOT_AVAILABLE, ROOM_OUT_OF_SERVICE, ROOM_NOT_CLEAN, CAPACITY_EXCEEDED, GUEST_NOT_FOUND, BOOKING_NOT_FOUND, INVALID_STATUS_TRANSITION, CHECK_IN_NOT_ALLOWED_TODAY, CANCELLATION_NOT_ALLOWED, CHECK_OUT_DATE_NOT_SUPPORTED, SERVICE_NOT_AVAILABLE, DUPLICATE_SERVICE_ITEM, BILL_CHANGED, EMAIL_ALREADY_EXISTS, UNAUTHENTICATED, ACCESS_DENIED, RESOURCE_BUSY, ROOM_ON_HOLD, HOLD_EXPIRED, INVALID_HOLD, HOLD_SERVICE_UNAVAILABLE, INTERNAL_ERROR.

## 12. Danh sách API và dữ liệu gửi/nhận

### 12.1 Auth và Guest — M3

| Method + URL | Quyền | Request | Response thành công |
|---|---|---|---|
| GET /api/auth/csrf | Public | Không | 200 {token, headerName} |
| POST /api/auth/register | Public + CSRF | {email,password,fullName,phone} | 201 GuestResponse |
| POST /api/auth/login | Public + CSRF | {email,password} | 200 UserResponse và cookie phiên |
| POST /api/auth/logout | Đã đăng nhập + CSRF | Không | 204 |
| GET /api/auth/me | Đã đăng nhập | Không | 200 UserResponse |
| GET /api/guests/me | GUEST | Không | 200 GuestResponse |
| PATCH /api/guests/me | GUEST + CSRF | {fullName,phone}; chỉ cập nhật trường được gửi | 200 GuestResponse |
| POST /api/guests | FRONT_DESK + CSRF | {fullName,phone,email?} | 201 GuestResponse |
| GET /api/guests?keyword=...&page=0&size=20 | FRONT_DESK | keyword tìm tên/phone/email; không rỗng | 200 trang GuestResponse |

UserResponse: {id,email,role,guestId}; guestId null cho nhân viên. GuestResponse: {id,fullName,phone,email,hasAccount}. Không trả passwordHash. Email đăng nhập không sửa qua profile API V1. Độ dài password 8–128; fullName 1–150; phone 8–20 ký tự số, khoảng trắng, + hoặc -; email tối đa 254 và đúng định dạng; trim text trước kiểm tra (không trim password).

### 12.2 Phòng — M2

| Method + URL | Quyền | Request | Response |
|---|---|---|---|
| GET /api/rooms/available | Public | query checkInDate,checkOutDate,guestCount,page,size | 200 trang AvailableRoomResponse |
| GET /api/rooms/{roomId} | Public | ID | 200 RoomResponse |

RoomResponse: {id,roomNumber,roomType,capacity,nightlyRate,currency,roomStatus}. AvailableRoomResponse thêm {checkInDate,checkOutDate,nightCount,roomAmount}. Không công khai thông tin khách hoặc booking liên quan.

### 12.2a Giữ phòng tạm — M2 API, M3 Redis

| Method + URL | Quyền | Request | Response |
|---|---|---|---|
| POST /api/room-holds | GUEST, FRONT_DESK + CSRF | {roomId,checkInDate,checkOutDate,guestCount} | 201 RoomHoldResponse |
| POST /api/room-holds/{holdToken}/release | Chủ hold + CSRF | Không | 204 nếu đã xóa hoặc đã hết hạn; 403 nếu token còn tồn tại thuộc người khác |

RoomHoldResponse: {holdToken,roomId,checkInDate,checkOutDate,guestCount,expiresAt}. holdToken là chuỗi ngẫu nhiên khó đoán, không ghi log; URL release phải được che token trong access log. M1 lưu trong bộ nhớ trong luồng đặt phòng, không đưa vào URL trang. Token không thay thế kiểm tra phiên và quyền sở hữu.

### 12.3 Booking — M2

| Method + URL | Quyền | Request | Response |
|---|---|---|---|
| POST /api/bookings | GUEST, FRONT_DESK + CSRF | CreateBookingRequest | 201 BookingResponse |
| GET /api/bookings | GUEST: chỉ của mình; FRONT_DESK: tất cả | query bookingStatus?,checkInDate?,page,size | 200 trang BookingResponse |
| GET /api/bookings/{bookingId} | Chủ booking hoặc FRONT_DESK | ID | 200 BookingResponse |
| POST /api/bookings/{bookingId}/cancel | Chủ booking hoặc FRONT_DESK + CSRF | Không | 200 BookingResponse |
| POST /api/bookings/{bookingId}/check-in | FRONT_DESK + CSRF | Không | 200 BookingResponse |

CreateBookingRequest:

```json
{
  "roomId": 1,
  "holdToken": "token-nhan-tu-api-giu-phong",
  "checkInDate": "2026-11-01",
  "checkOutDate": "2026-11-03",
  "guestCount": 2
}
```

FRONT_DESK phải gửi thêm guestId của hồ sơ đã tìm/tạo. GUEST không được gửi guestId, nếu có trả 400; backend lấy từ phiên đăng nhập. Không nhận trạng thái, giá hay createdByUserId từ client.

BookingResponse: {id,bookingCode,guestId,guestName,roomId,roomNumber,checkInDate,checkOutDate,guestCount,nightCount,nightlyRate,roomAmount,currency,bookingStatus,createdAt,checkedInAt,checkedOutAt,cancelledAt}. Những thời điểm chưa có trả null.

Cancel lặp lại với CANCELLED trả trạng thái cũ; check-in lặp với CHECKED_IN trả trạng thái cũ; không ghi lại thời điểm. POST tạo booking không tự idempotent trong V1: M1 khóa nút khi gửi, không tự retry khi timeout; kiểm tra danh sách trước khi gửi lại. Khóa phòng vẫn phải bảo đảm không tạo hai booking trùng.

### 12.4 Dịch vụ — M2

| Method + URL | Quyền | Request | Response |
|---|---|---|---|
| GET /api/hotel-services | Public | page,size | 200 trang HotelServiceResponse; chỉ active |
| POST /api/bookings/{bookingId}/service-orders | Chủ booking hoặc FRONT_DESK + CSRF | {items:[{hotelServiceId,quantity}]} | 201 ServiceOrderResponse |
| GET /api/bookings/{bookingId}/service-orders | Chủ booking hoặc FRONT_DESK | page,size | 200 trang ServiceOrderResponse |
| POST /api/service-orders/{serviceOrderId}/cancel | FRONT_DESK + CSRF | Không | 200 ServiceOrderResponse; hủy lặp trả đơn đã hủy khi booking còn CHECKED_IN |

HotelServiceResponse: {id,name,unitPrice,currency}. ServiceOrderResponse: {id,bookingId,serviceOrderStatus,items,totalAmount,currency,createdAt}; item gồm {id,hotelServiceId,serviceName,quantity,unitPrice,lineAmount}. Không nhận unitPrice hoặc totalAmount trong request.

POST tạo đơn dịch vụ không có tự retry/idempotency trong V1; M1 khóa nút và kiểm tra danh sách khi kết quả mạng không rõ. Ghi nhận đây là hạn chế cần cải tiến bằng idempotency key trước triển khai thực tế.

### 12.5 Housekeeping — M2

| Method + URL | Quyền | Request | Response |
|---|---|---|---|
| GET /api/housekeeping/rooms | HOUSEKEEPING, FRONT_DESK | housekeepingStatus?,page,size | 200 trang HousekeepingRoomResponse |
| PATCH /api/housekeeping/rooms/{roomId}/status | HOUSEKEEPING, FRONT_DESK + CSRF | {housekeepingStatus} | 200 HousekeepingRoomResponse |

HousekeepingRoomResponse: {roomId,roomNumber,roomStatus,housekeepingStatus,isOccupied}. isOccupied dựa booking CHECKED_IN, không dựa ngày tìm kiếm. Không trả tên/phone khách. V1 chỉ dọn phòng sau trả phòng; chặn cập nhật dọn cho phòng có booking CHECKED_IN. Check-in và cập nhật housekeeping cùng khóa Room để tránh xung đột.

### 12.6 Hóa đơn và check-out — M2

| Method + URL | Quyền | Request | Response |
|---|---|---|---|
| GET /api/bookings/{bookingId}/bill-preview | Chủ booking hoặc FRONT_DESK | Không | 200 BillPreviewResponse |
| POST /api/bookings/{bookingId}/check-out | FRONT_DESK + CSRF | {expectedTotalAmount,paymentMethod:"CASH",paymentConfirmed:true} | 200 InvoiceResponse |
| GET /api/me/invoices | GUEST | page,size | 200 trang InvoiceResponse |
| GET /api/invoices/{invoiceId} | Chủ hóa đơn hoặc FRONT_DESK | ID | 200 InvoiceResponse |

BillPreviewResponse: {bookingId,nightCount,roomAmount,serviceAmount,totalAmount,currency,canCheckOut}. Preview cho booking CONFIRMED hoặc CHECKED_IN; booking CANCELLED bị từ chối; CHECKED_OUT xem hóa đơn đã lưu.

InvoiceResponse: {id,invoiceNumber,bookingId,guestName,roomNumber,checkInDate,checkOutDate,items,roomAmount,serviceAmount,totalAmount,currency,paymentStatus,paymentMethod,paidAt}; item gồm {itemType,description,quantity,unitPrice,lineAmount}. Khách xem snapshot lịch sử, không lấy tên/giá hiện tại để dựng lại hóa đơn.

## 13. Luồng giao diện cần hoàn thành

1. Khách: tìm phòng → đăng ký/đăng nhập nếu cần → giữ phòng tạm → xác nhận đặt → xem booking → hủy trước ngày nhận nếu muốn.
2. Walk-in: lễ tân đăng nhập → tìm/tạo Guest → tìm phòng từ hôm nay → giữ phòng tạm → tạo booking với guestId và holdToken → check-in khi phòng sạch.
3. Trong kỳ ở: khách/lễ tân tạo đơn ORDERED → lễ tân hủy nếu cần trước check-out. Không có bước xác nhận hoàn thành dịch vụ.
4. Trả phòng: lễ tân mở preview → kiểm tra danh sách dịch vụ ORDERED → xác nhận tiền → check-out → xem hóa đơn; khách xem trong MyInvoicesPage.
5. Housekeeping: xem phòng DIRTY → IN_PROGRESS → CLEAN; phòng nhận khách tiếp khi CLEAN.

M1 phải có trạng thái loading, empty, lỗi validation, 401, 403, 409. Khi 401 chuyển đến login và giữ thông tin tìm phòng hợp lệ; không tự gửi lại POST tạo dữ liệu. Mỗi vai trò chỉ thấy hành động được phép nhưng backend vẫn kiểm tra quyền.

## 14. Test plan — M4

| Nhóm | Trường hợp bắt buộc | Loại test |
|---|---|---|
| Khoảng ngày | Bằng nhau, đảo ngày, trước hôm nay; ngày trả của booking cũ bằng ngày nhận mới và ngược lại | Unit policy/Service |
| Giao nhau | Trùng hoàn toàn; nằm trong; bao ngoài; giao đầu/cuối; khác phòng; CANCELLED không chặn | Unit + DataJpaTest |
| Điều kiện phòng | Thiếu phòng, OUT_OF_SERVICE, vượt capacity; DIRTY được đặt nhưng không check-in | Unit + API |
| Booking đồng thời | Hai transaction khác connection cùng đặt một phòng/cùng ngày: đúng một thành công, một 409, chỉ một booking | Integration MySQL |
| Redis hold | TTL, hai người giữ cùng phòng, sai chủ, token sai/hết hạn, release an toàn, Redis lỗi 503, hold hết hạn trong lúc booking commit | Integration MySQL + Redis |
| Auth | Đăng ký trùng email; password không lộ; login sai; hết phiên; logout; CSRF; đổi session sau login | Integration/API |
| Quyền | Khách A không xem/sửa booking, đơn, hóa đơn khách B; housekeeping không đọc dữ liệu khách | Integration/API |
| Trạng thái | Hủy trước ngày nhận; không hủy sau check-in; check-in sai ngày; thao tác lặp | Unit + API |
| Dịch vụ | Quantity sai; item trùng; dịch vụ inactive; gọi trước check-in; chuyển trạng thái sai; hủy sau check-out bị chặn; snapshot giá | Unit + API |
| Hóa đơn | Đúng số đêm, tổng dịch vụ; cộng đơn ORDERED; không cộng đơn hủy; BILL_CHANGED | Unit + integration |
| Atomicity | Lỗi lưu invoice rollback booking/room; check-out lặp/đồng thời không tạo invoice kép | Integration |
| Race dịch vụ/check-out | Tạo/hủy đơn và check-out đồng thời được tuần tự hóa bằng khóa Booking | Integration |
| Housekeeping | Đúng chuỗi trạng thái; không dọn khi đang ở; check-out chuyển DIRTY | Unit + API |
| UI end-to-end | Khách đặt → lễ tân nhận → gọi dịch vụ → thu tiền/trả → dọn sạch | Manual/E2E |

Dùng Clock cố định để test hôm nay/ngày nhận. Mock Repository cho unit test; dùng @DataJpaTest cho query. Test khóa/concurrency với MySQL thật qua Testcontainers, không chỉ H2; test không nằm toàn bộ trong một transaction bao ngoài làm mất tình huống đồng thời. M4 phối hợp M3 chuẩn bị fixture và cleanup độc lập.

Tiêu chí đạt: tất cả luồng bắt buộc thành công; lỗi quyền/đặt trùng/tính sai tiền/ghi dữ liệu một phần phải được sửa; có kết quả test và hướng dẫn tái hiện. GitHub Actions do M4 phụ trách, M1 cung cấp lệnh typecheck/build frontend, M2/M3 cung cấp cấu hình build/test backend và MySQL/Redis test. Workflow chạy khi push/PR: cài dependency từ lockfile, typecheck/build frontend, Maven verify backend, lưu kết quả test/coverage và fail khi test lỗi. Coverage 90% trong kế hoạch tuần 8 hiện là ví dụ; nếu nhóm dùng làm quality gate thì chốt phạm vi đo và cấu hình JaCoCo, không coi con số này là yêu cầu giảng viên đã xác nhận.

## 15. Clean Code và tài liệu theo phân công ảnh

| Người | Công việc và bằng chứng |
|---|---|
| M1 | Quy ước frontend; tách BookingForm/CheckoutForm theo trách nhiệm; constants.ts cho cấu hình phù hợp; cập nhật state bất biến; ví dụ trước/sau; bảng key UI/i18n nếu môn học yêu cầu |
| M2 | Constants.java cho hằng số nghiệp vụ, MAX_HOLD_MINUTES cấu hình đồng bộ với M3; quy ước Controller/Service/DTO; tách createBooking và checkOut thành bước rõ ràng; guard clauses theo điều kiện; enum/hằng số có nghĩa; ví dụ trước/sau; sơ đồ kiến trúc/package/DI/exception/sequence |
| M3 | RedisHoldService, cấu hình TTL và ví dụ kiểm tra/xóa token nguyên tử; quy ước Repository/Entity; cấu hình bí mật qua biến môi trường; query tham số hóa; tránh nối input vào SQL; kiểm soát collection mutable theo ngữ cảnh JPA; ERD và sơ đồ bảo mật/khóa; ví dụ trước/sau |
| M4 | TestConstants.java, GitHub Actions chạy test tự động; checklist chung; fixture/builder rõ ràng; mỗi test một hành vi; test constants khi thực sự dùng lại; tổng hợp class/package diagram, traceability và báo cáo |

Không ép mọi giá trị vào một Constants.java lớn. Dùng enum cho trạng thái; mã lỗi trong ErrorCode; cấu hình trong application properties/env. Theo kế hoạch tuần 8, chuẩn bị Constants.java, constants.ts và TestConstants.java, chỉ chứa giá trị phù hợp, không gom password, mọi chuỗi hoặc toàn bộ HTTP code vào đó. Không áp dụng guard clause/defensive copying máy móc nếu làm sai transaction hoặc ánh xạ JPA.

Tài liệu dự kiến trong docs/: scope-and-rules.md, api-contract.yaml, database-erd, architecture, booking-sequence, security-and-session, test-plan, traceability-matrix, clean-code-report. Sơ đồ phải phản ánh code cuối cùng; M4 tích hợp, từng người chịu trách nhiệm phần mình.

## 16. Kế hoạch triển khai theo mốc


| Mốc | M1 | M2 | M3 | M4 | Điều kiện hoàn thành |
|---|---|---|---|---|---|
| 0. Chốt hợp đồng | Wireframe và field UI | API/DTO/policy | ERD/auth/khóa | Test case và câu hỏi biên | Nhóm duyệt V1, tên trường thống nhất |
| 1. Dựng nền | Project, routing, API client | Spring project, lỗi chung, Service skeleton | Migration, seed, Repository, auth | Test setup, fixture | Mọi máy chạy được, DB có mẫu, login/session hoạt động |
| 2. Đặt phòng | Search, booking, danh sách cá nhân | Giữ phòng, tìm phòng/create/cancel | Query, khóa Room, Redis TTL hold | Test overlap/concurrency/hold/quyền | Đặt từ UI xuống DB; không trùng khi đồng thời |
| 3. Lưu trú | Front desk và dịch vụ | Check-in, service order | Hoàn thiện query/security | Test trạng thái và giá snapshot | Nhận phòng và gọi/hủy dịch vụ |
| 4. Trả và dọn | Checkout, invoice, housekeeping | Billing/check-out/dọn | Ràng buộc hóa đơn/khóa | Test tiền, rollback, concurrency | Hoàn tất luồng toàn hệ thống |
| 5. Bàn giao | Rà UI | Rà nghiệp vụ/API | Rà DB/bảo mật | Regression và tổng hợp | Test đạt, docs đúng code, demo được từ DB mới |

Việc đầu tiên mỗi người làm ở mốc 0–1:

- M1 tạo wireframe trang tìm phòng/đặt phòng, danh sách field và API client mẫu.
- M2 tạo project backend, chốt DTO API tìm/tạo booking và chữ ký RoomAvailabilityPolicy.
- M3 tạo ERD, migration các bảng nền, RoomEntity/BookingEntity, chữ ký query/khóa, RedisHoldService và auth/session.
- M4 lập bộ test khoảng ngày và ma trận quyền từ bản này; dựng cấu trúc test và workflow GitHub Actions.

## 17. Làm việc với Git và bàn giao

- main là nhánh tích hợp; mỗi việc dùng feature/<ten-ngan>, ví dụ feature/booking-api.
- Commit theo mục đích: feat, fix, refactor, test, docs. Không commit .env, password, database dump chứa thông tin thật.
- Pull request nêu hành vi thay đổi, API/schema bị ảnh hưởng và kiểm tra đã chạy. Ít nhất một người review trước merge; ưu tiên người phụ thuộc phần đó.
- Mọi PR đổi API hoặc database cập nhật tài liệu và báo M1/M4 trước tích hợp.
- Mỗi phần bàn giao có cách chạy, dữ liệu mẫu, ví dụ request/response, lỗi dự kiến và kiểm tra đã thực hiện.
- Nếu thiếu dependency của người khác, dùng mock theo hợp đồng đã chốt, không tự đổi field để chạy riêng.
- Khi lỗi mạng không rõ thao tác POST đã thành công chưa, kiểm tra trạng thái/danh sách; không tự động gửi lại đơn dịch vụ.

## 18. Checklist nghiệm thu chung

- [ ] Cả 4 thành viên chạy được từ clone mới bằng hướng dẫn README.
- [ ] Migration tạo DB mới và seed thành công, không sửa schema tay.
- [ ] Đăng ký/login/logout/CSRF và phân quyền đúng.
- [ ] Khách tự đặt và lễ tân đặt hộ chạy được.
- [ ] Tìm phòng/đặt phòng đúng ngày biên; concurrency không tạo booking trùng.
- [ ] Check-in kiểm tra ngày, phòng sạch và trạng thái.
- [ ] Đơn dịch vụ tuân thủ chuyển trạng thái và giá snapshot.
- [ ] Check-out cộng đơn ORDERED, bỏ đơn CANCELLED, chốt đúng tiền, một hóa đơn/booking.
- [ ] Redis giữ phòng đúng TTL/chủ sở hữu; MySQL vẫn chống đặt trùng khi hold hết hạn.
- [ ] Frontend TypeScript build được; CI GitHub Actions chạy test MySQL/Redis thành công.
- [ ] Khách chỉ thấy dữ liệu của mình; housekeeping không lộ dữ liệu khách.
- [ ] Trả phòng chuyển DIRTY; dọn xong chuyển CLEAN.
- [ ] Test bắt buộc đạt, lỗi quan trọng được sửa, tài liệu khớp code.
- [ ] Có demo đầy đủ từ tìm phòng đến trả phòng và dọn phòng.
