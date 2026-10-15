# Quy tắc chuyển trạng thái V1

Căn cứ V1 mục 7–9.1, 12.3–12.6. Đây là quy tắc cần Service thực thi khi có Entity/Repository,
chưa có Service test hay transaction thật. Không thêm tầng domain/adapter để thay phần M3.

## Booking và tiền phòng

| Thao tác | Trạng thái đầu → cuối | Điều kiện | Lặp / từ chối |
|---|---|---|---|
| Create | Chưa có → CONFIRMED | ACTIVE, capacity đủ; ngày nhận >= hôm nay, ngày trả > nhận; hold hợp lệ; không overlap | Không idempotent; không tự retry POST |
| Cancel | CONFIRMED → CANCELLED | Chủ hoặc FRONT_DESK; hôm nay < checkInDate | CANCELLED trả cũ, không ghi lại cancelledAt, kể cả khi ngày đã qua; trạng thái khác bị từ chối |
| Check-in | CONFIRMED → CHECKED_IN | FRONT_DESK, đúng ngày nhận, room ACTIVE và CLEAN | CHECKED_IN trả cũ, không ghi lại checkedInAt; không reapply side effect |
| Check-out | CHECKED_IN → CHECKED_OUT | FRONT_DESK; ngày cho phép; CASH/confirmed; tổng backend = expected | CHECKED_OUT trả invoice cũ, không thu tiền lại; thiếu invoice là lỗi toàn vẹn, không tự tạo mới |

Kiểm tra phiên/role/ownership và Bean Validation trước trả kết quả lặp.
Chỉ CONFIRMED/CHECKED_IN chặn [checkInDate,checkOutDate); CANCELLED/CHECKED_OUT không chặn.
Trả sớm không giảm roomAmount, không đổi ngày đã đặt, không hỗ trợ gia hạn/trả muộn.
Biên ngày checkout bao gồm ngày trả là đề xuất D08, không nhầm với khoảng chặn lịch nửa mở.

## Service order

| Thao tác | Trạng thái | Điều kiện |
|---|---|---|
| Create | Chưa có → ORDERED | Khóa Booking cha; CHECKED_IN; hôm nay <= checkOutDate; GUEST đúng chủ hoặc FRONT_DESK |
| Cancel | ORDERED → CANCELLED | FRONT_DESK; khóa Booking cha; booking vẫn CHECKED_IN |
| Cancel lặp | CANCELLED → CANCELLED | Chỉ khi booking vẫn CHECKED_IN; không side effect |
| Sau check-out | Không tạo/hủy/sửa | Kể cả đơn đã CANCELLED, hủy lặp sau check-out vẫn bị chặn |

Ít nhất một item; hotelServiceId không lặp; quantity > 0; dịch vụ active; snapshot tên/giá.
Không sửa item sau tạo. Không PLACED/COMPLETED/progress trong V1.
Mọi ORDERED cộng hóa đơn, CANCELLED không cộng. Tạo/hủy khóa cùng Booking với check-out.
V1 không quy định thêm điều kiện ngày khi hủy đơn; không tự cấm hủy quá ngày trả nếu booking vẫn CHECKED_IN.

## Housekeeping

| Actor | Đầu → cuối | Điều kiện |
|---|---|---|
| HOUSEKEEPING/FRONT_DESK | DIRTY → IN_PROGRESS | Khóa Room, không có booking CHECKED_IN |
| HOUSEKEEPING/FRONT_DESK | IN_PROGRESS → CLEAN | Khóa Room, không có booking CHECKED_IN |
| HOUSEKEEPING/FRONT_DESK | Cùng trạng thái | Không occupied; trả cũ, không thay updatedAt |
| Hệ thống check-out | CLEAN → DIRTY | Cùng transaction với invoice PAID và booking CHECKED_OUT |

CLEAN → DIRTY từ API và DIRTY → CLEAN bị từ chối.
Chặn cập nhật khi occupied trước cả nhánh cùng trạng thái; trạng thái đang ở dựa CHECKED_IN, không theo ngày.
Check-in khóa cùng Room nên không chen vào cập nhật dọn.
DIRTY vẫn đặt được; housekeeping sạch không thay thế kiểm tra ACTIVE/lịch phòng.

## Hold và hóa đơn

- Hold không có bảng/status MySQL: acquire TTL 15 phút; không gia hạn; release nguyên tử.
  Sau booking commit cleanup đúng token best effort. Nếu TTL hết trong transaction, khóa MySQL vẫn bảo vệ đến commit.
- Preview CONFIRMED/CHECKED_IN là tính tạm không persist; canCheckOut chỉ true nếu trạng thái/ngày cho phép.
  CANCELLED bị từ chối. CHECKED_OUT chuyển client sang invoice API; HTTP cụ thể của preview đã kết thúc chờ D08.
- Invoice chỉ PAID; paymentMethod chỉ CASH. Snapshot phòng/khách/ngày/item được lưu một lần.
- Check-out transaction: khóa Room → Booking → kiểm tra/lặp → tính roomAmount + ORDERED →
  so expected (BILL_CHANGED kèm currentTotalAmount nếu khác) → lưu invoice/items → booking CHECKED_OUT → room DIRTY.
- Lỗi bất kỳ rollback toàn bộ; UNIQUE invoice.booking_id + khóa Booking bảo vệ invoice kép.
- Không bắt lỗi cleanup afterCommit rồi báo booking thất bại; không retry POST tạo dữ liệu tự động.

## Phân biệt bằng chứng

Đã có test độc lập RoomAvailabilityPolicy: ngày biên, leap/year boundary, trạng thái chặn, capacity, tiền phòng.
Bảng chuyển trạng thái trên chưa được tự động thực thi: cần Service thật để test idempotency, timestamp,
snapshot, ownership và tổng chỉ ORDERED. Không tạo policy trùng Service chỉ để test một bản sao của tài liệu.
Khóa, rollback, TTL và race cần MySQL/Redis integration, không thể chứng minh bằng mock.
