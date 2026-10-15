# Ma trận quyền V1 — hợp đồng mục tiêu

Căn cứ V1 mục 10–12. Đây là yêu cầu cần thực thi/test khi có security và principal M3, chưa phải kết quả test phân quyền.
“Public” áp dụng cả người chưa đăng nhập và mọi role; “—” bị từ chối.
Route cho phép GUEST phải kiểm tra liên kết guest.userId/guestId từ principal, không chỉ role hay ID request.

| Method + URL | Public | GUEST | FRONT_DESK | HOUSEKEEPING | CSRF cho mutation |
|---|---|---|---|---|---|
| GET /api/rooms/available | Có | Có | Có | Có | — |
| GET /api/rooms/{roomId} | Có | Có | Có | Có | — |
| POST /api/room-holds | — | Có | Có | — | Có |
| POST /api/room-holds/{holdToken}/release | — | Chủ hold | Chủ hold | — | Có |
| POST /api/bookings | — | guestId từ phiên; cấm trường guestId | guestId hợp lệ bắt buộc | — | Có |
| GET /api/bookings | — | Chỉ của mình | Tất cả | — | — |
| GET /api/bookings/{bookingId} | — | Chủ booking | Có | — | — |
| POST /api/bookings/{bookingId}/cancel | — | Chủ booking | Có | — | Có |
| POST /api/bookings/{bookingId}/check-in | — | — | Có | — | Có |
| GET /api/hotel-services | Có | Có | Có | Có | — |
| POST /api/bookings/{bookingId}/service-orders | — | Chủ booking | Có | — | Có |
| GET /api/bookings/{bookingId}/service-orders | — | Chủ booking | Có | — | — |
| POST /api/service-orders/{serviceOrderId}/cancel | — | — | Có | — | Có |
| GET /api/housekeeping/rooms | — | — | Có | Có | — |
| PATCH /api/housekeeping/rooms/{roomId}/status | — | — | Có | Có | Có |
| GET /api/bookings/{bookingId}/bill-preview | — | Chủ booking | Có | — | — |
| POST /api/bookings/{bookingId}/check-out | — | — | Có | — | Có |
| GET /api/me/invoices | — | Chỉ của mình | — | — | — |
| GET /api/invoices/{invoiceId} | — | Chủ hóa đơn | Có | — | — |

## API M3 cần để tích hợp

| Method + URL | Quyền | CSRF |
|---|---|---|
| GET /api/auth/csrf | Public | Không |
| POST /api/auth/register | Public, luôn tạo GUEST | Có |
| POST /api/auth/login | Public | Có |
| POST /api/auth/logout | Đã đăng nhập, mọi role | Có |
| GET /api/auth/me | Đã đăng nhập, mọi role | Không |
| GET /api/guests/me | GUEST | Không |
| PATCH /api/guests/me | GUEST | Có |
| POST /api/guests | FRONT_DESK | Có |
| GET /api/guests | FRONT_DESK | Không |

## Quy tắc bảo mật phải kiểm tra ở backend

- Chưa đăng nhập: 401 UNAUTHENTICATED; sai role/CSRF: 403 ACCESS_DENIED.
  Thứ tự filter có thể trả 403 trước 401 cho request thiếu CSRF; M3 cần thống nhất test.
- GUEST A truy cập booking/order/invoice của B: 404, không lộ PII hoặc xác nhận tài nguyên tồn tại.
  Mã 404 của invoice/order chưa chốt (D02), không tự dùng BOOKING_NOT_FOUND cho mọi loại.
- Release: token còn tồn tại của người khác → 403; hết hạn/đã xóa → 204; không được xóa hold mới sau TTL của hold cũ.
- FRONT_DESK được thao tác hộ nhưng vẫn phải tuân thủ trạng thái, ngày, giá và khóa.
- HOUSEKEEPING chỉ có dữ liệu roomId/roomNumber/roomStatus/housekeepingStatus/isOccupied ở API dọn phòng; không tên/phone/guestId/bookingId.
- Cookie phiên không phải Redis room-hold; không JWT; không làm yếu session/CSRF của M3.
- GET không thay đổi dữ liệu. Thành công DTO trực tiếp, lỗi cùng ApiError; không redirect HTML.

## Test cần sau khi M3 bàn giao

Với mỗi route bảo vệ: chưa login, role sai, đúng role, GUEST A/B, mutation thiếu/sai/đúng CSRF.
Test list phải xác minh lọc ở DB, không đọc toàn bộ rồi lọc/paginate trong Java.
Test repeat phải kiểm tra quyền trước trả dữ liệu cũ.
Bộ MockMvc standalone hiện tại chỉ test handler/validation, không có filter chain và không chứng minh ma trận này đã được enforce.
