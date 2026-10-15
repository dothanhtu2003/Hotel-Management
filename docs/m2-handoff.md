# Bàn giao M2 — phần độc lập, chưa hoàn tất backend nghiệp vụ

Căn cứ: [V1](../Hotel_Project_Full_Plan_V1(1).md), đã đối chiếu các mục 5, 7–12, 14–18.
Project hiện chỉ có khung Spring Boot và nền tảng M2; chưa có Entity/Repository/principal/RedisHoldService của M3.
Giữ Java 21, Spring Boot 4.1.1 và JUnit 6 theo xác nhận người dùng ngày 15/10/2026.

## Tài liệu để review và gửi nhóm

- [Checklist P0 gửi M3](m2-m3-dependencies.md): Room/Booking/Guest, principal, Redis hold; P1 là dịch vụ/hóa đơn/dọn phòng.
- [OpenAPI 3.0.3](api-contract.yaml): 19 thao tác M2 + 9 thao tác M3, schema DTO, tiền VND, phân trang, role, session + CSRF.
- [Ma trận quyền](m2-permission-matrix.md): quyền từng route, ownership, 401/403/404 và giới hạn dữ liệu housekeeping.
- [Quy tắc trạng thái](m2-state-rules.md): điều kiện ngày/trạng thái, lặp không side effect, thứ tự khóa và transaction cần thực thi.
- [Quyết định cần chốt](m2-decisions.md): mã lỗi framework, 404 invoice/order, images MySQL/Redis, principal/Redis và các điểm biên.

OpenAPI mô tả hợp đồng mục tiêu; việc lint hợp lệ không chứng minh endpoint đã tồn tại hoặc quyền đã được enforce.
Các mục M3 chỉ là đặc tả tích hợp; M2 không triển khai thay auth/security/schema.

## Phần code M2 đã có

| Thành phần | Vai trò |
|---|---|
| config/BusinessTimeConfiguration | Clock Asia/Ho_Chi_Minh; test dùng Clock.fixed |
| Constants, enums | VND, phân trang và trạng thái chung; TTL 15 phút sẽ lấy từ cấu hình duy nhất của M3 |
| dto | Request/response theo V1; @Valid; PageQuery/PageResponse; query ngày ISO |
| dto/VndSerializer | BigDecimal response thành chuỗi số nguyên, không tự làm tròn |
| dto/CreateBookingRequest | Phân biệt guestId vắng với guestId:null; kiểm tra role/guestId cần Service thật |
| policy/RoomAvailabilityPolicy | Validate ngày/count/capacity/ACTIVE; overlaps [in,out); trạng thái chặn; số đêm/tiền phòng |
| exception | ApiError, BusinessException, ErrorCode, handler MVC; không lộ token/rejectedValue/SQL/stack trace |

DTO là dữ liệu tại biên API; Entity là dữ liệu JPA do M3 cung cấp.
Controller sau này dùng @RequestBody @Valid / @ModelAttribute @Valid, Service kiểm tra quyền/nghiệp vụ trong transaction.
Không có Controller/Service nghiệp vụ, Entity giả, Repository giả hoặc tầng adapter để né dependency M3.

## Sửa sau rà soát

- V1: cập nhật quyết định JUnit 6, thống nhất hotelServiceId thay serviceId và bổ sung 204 release hold.
- Rút lại giả định BOOKING_NOT_FOUND dùng chung invoice/order; đưa vào quyết định D02.
- RESOURCE_BUSY chỉ bắt CannotAcquireLockException, JPA LockTimeoutException, JPA PessimisticLockException.
  Không bắt toàn bộ ConcurrencyFailureException hoặc PessimisticLockingFailureException.
- Test HTTP xác minh optimistic locking, transaction serialization, concurrency chung và lỗi integrity vẫn trả 500 INTERNAL_ERROR.
  Chưa xác minh MySQL/Hibernate thực tế bọc/translate lỗi lúc flush/commit; D05 cần integration.
- Cập nhật lệnh test đủ 5 class và thư mục chạy rõ ràng. Không dùng lệnh này để tuyên bố full suite đã qua.
- Thêm test policy ngày thiếu/ngày biên chứa nhau, leap/year boundary, count <= 0 và giá database sai/giá 0.
- Test OpenAPI đối chiếu tên field DTO, enum, local refs, số thao tác, security của mutation, page và BILL_CHANGED.

## Lệnh chạy đã kiểm tra

Từ PowerShell trên máy này; thay đường dẫn JDK/workspace nếu dùng máy khác.
JAVA_HOME hiện mặc định trỏ Java 17 nên cần chọn JDK 21 cho tiến trình shell.

~~~powershell
Set-Location -LiteralPath 'E:\Hotel project\backend'
$env:JAVA_HOME = 'C:\Users\thuho\.jdks\temurin-21.0.12.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -B '-Dtest=RoomAvailabilityPolicyTest,BusinessDtoTest,GlobalExceptionHandlerTest,ApiErrorMvcTest,OpenApiContractTest' test
~~~

Lint OpenAPI từ root repository (cần Node/npm; lần đầu tải CLI vào npm cache):

~~~powershell
Set-Location -LiteralPath 'E:\Hotel project'
npx.cmd --yes @redocly/cli@2.64.0 lint docs/api-contract.yaml --extends minimal
~~~

Không cần Docker/MySQL/Redis cho các lệnh test độc lập trên.
Không thay biến môi trường toàn máy, không sửa POM/BOM/security/database để chạy riêng.

## Bằng chứng và giới hạn

| Phạm vi | Kết quả đã chạy | Điều chưa chứng minh |
|---|---|---|
| Compile production + test source | Maven Java 21 BUILD SUCCESS | Chưa chạy ứng dụng Spring Boot hoàn chỉnh |
| RoomAvailabilityPolicyTest | 26 test đạt | Chưa gọi query JPA hoặc khóa Room |
| BusinessDtoTest | 7 test đạt | Chưa enforce toàn bộ OpenAPI request tại Controller thật |
| GlobalExceptionHandlerTest | 4 test đạt | Không phải kiểm thử translation Hibernate/MySQL |
| ApiErrorMvcTest | 10 test đạt, controller chỉ nằm trong test | Không có security filter chain/CSRF/session/DB |
| OpenApiContractTest | 5 test đạt | Không test endpoint runtime hoặc quyền ownership |
| Redocly 2.64.0 lint, preset minimal | Hợp lệ, 0 lỗi/0 cảnh báo | Không chứng minh backend phục vụ hợp đồng |
| Full SpringBootTest/MySQL/Redis | Chưa chạy | Migration, startup, Redis TTL, transaction, concurrency, rollback |

Tổng bộ độc lập: 52 test, 0 failure, 0 error, 0 skipped.
Báo cáo máy hiện tại: backend/target/surefire-reports, m2-tests.log và openapi-lint.log (target không commit).
Maven có cảnh báo metadata 401 từ repository Flyway/Red Gate phụ nhưng build thành công qua Central;
Mockito có cảnh báo self-attach agent; test case serialization dùng API Spring đã deprecated để kiểm tra không ánh xạ nhầm.

## Phần còn bị chặn và bước nhận code M3

Chờ checklist P0; trước hết cần Entity/Repository thật, principal và RedisHoldService cùng cấu hình chạy.
Sau khi nhận: đối chiếu chữ ký/field/enum/query/khóa/security với V1 và OpenAPI, ghi khác biệt để nhóm chốt,
tận dụng code phù hợp rồi viết Controller/Service thật theo Controller → Service → Repository.
Không ép M3 tạo một tầng provider mới nếu principal sẵn có dùng được.

Tiếp theo viết unit test Service với mock các dependency thật và integration MySQL/Redis
cho đặt trùng, TTL, cleanup afterCommit, rollback và ownership/CSRF.
Dịch vụ/billing/housekeeping cần dependency P1. Các chuyển trạng thái hiện mới là hợp đồng,
chưa có Service thực thi hoặc bằng chứng test lặp/timestamp/PAID/ORDERED.
