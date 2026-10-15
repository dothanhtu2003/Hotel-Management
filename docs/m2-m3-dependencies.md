# Checklist gửi M3 — ưu tiên hoàn thành luồng đặt phòng

Đây là hợp đồng đề xuất, chưa phải code M3 đã có. M2 hiện chưa có Controller/Service nghiệp vụ.
Nguồn: [V1](../Hotel_Project_Full_Plan_V1(1).md) mục 6, 9, 9.1, 10, 12.2–12.3.
M3 gửi class/method thực tế; nếu tên khác nhưng cùng ý nghĩa, M2 đối chiếu để dùng lại, không tạo Entity/Repository song song.

## P0 — cần trước khi M2 viết search → hold → create/list/get/cancel booking

- [ ] Cung cấp RoomEntity, BookingEntity, GuestEntity và quan hệ User; getter/factory/setter thực tế.
  Room đủ giá/capacity/ACTIVE/housekeeping; Booking đủ guest/room/createdBy/ngày/count/status/snapshot/thời điểm.
  Dùng BigDecimal, LocalDate, Instant; enum chung com.hotelmanagement.enums (hoặc chốt một nguồn enum duy nhất).
- [ ] RoomRepository: Optional<RoomEntity> findById(Long roomId).
- [ ] RoomRepository: Optional<RoomEntity> findByIdForUpdate(Long roomId), @Lock(PESSIMISTIC_WRITE).
- [ ] RoomRepository: Page<RoomEntity> findAvailable(LocalDate checkInDate, LocalDate checkOutDate, int guestCount, Pageable pageable).
  ACTIVE + đủ capacity + NOT EXISTS booking chặn giao nhau; không loại DIRTY, không đọc Redis; count query cùng bộ lọc.
- [ ] BookingRepository: List<BookingEntity> findConflicts(Long roomId, LocalDate checkInDate, LocalDate checkOutDate).
  Chỉ cùng phòng, CONFIRMED/CHECKED_IN; existing.in < requested.out AND existing.out > requested.in.
  Chạy cùng transaction đang khóa Room, không REQUIRES_NEW/query cache.
- [ ] BookingRepository: Optional<BookingEntity> findById(Long bookingId), findByIdForUpdate(Long bookingId);
  Optional<Long> findRoomIdByBookingId(Long bookingId) để khóa Room trước Booking; roomId bất biến ở V1.
- [ ] BookingRepository: Page<BookingEntity> findVisible(Long guestId, BookingStatus status, LocalDate checkInDate, Pageable pageable).
  guestId nullable chỉ dành FRONT_DESK; GUEST luôn bị lọc theo principal; date/status nullable là không lọc.
- [ ] GuestRepository: Optional<GuestEntity> findById(Long guestId).
  Xác minh guest từ principal / hồ sơ FRONT_DESK chỉ định; không dựa email/phone để cho quyền.
- [ ] BookingRepository extends JpaRepository<BookingEntity, Long>: save; M2 chốt snapshot và createdBy trong transaction READ_COMMITTED.
  DTO được dựng trong transaction hoặc dùng fetch phù hợp để không lỗi lazy/N+1.
- [ ] Flyway + cấu hình MySQL từ biến môi trường, không ddl-auto=update/create; FK thật, index lịch phòng, seed.
  Xác nhận Java 21 / Boot 4.1.1; khóa được giữ đến commit. Gửi hướng dẫn chạy DB.

## P0 — principal và Redis hold

- [ ] Chốt cách M2 lấy userId, Role và guestId qua Authentication do M3 xác thực.
  Có thể dùng @AuthenticationPrincipal với principal M3 đã có; không bắt buộc thêm CurrentUserProvider.
  GUEST phải có guestId hợp lệ; FRONT_DESK có thể guestId=null.
- [ ] M3 cấu hình role, session cookie, CSRF, JSON 401/403 theo ApiError; không redirect HTML.
  Gửi tên principal/getter, cookie/header CSRF và cách test đăng nhập.
- [ ] RedisHoldService cung cấp chữ ký tương đương:
  - Duration holdTtl() — một cấu hình chung 15 phút.
  - Optional<HoldData> tryAcquire(Long userId, Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount).
  - HoldData requireValid(Long roomId, String token, Long userId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount).
  - ReleaseResult releaseOwned(String token, Long userId), kết quả RELEASED / EXPIRED / NOT_OWNER.
  - boolean compareAndDelete(Long roomId, String token).
- [ ] HoldData có token/userId/roomId/ngày/count/expiresAt; token khó đoán, toString/log che token.
  SET NX kèm TTL nguyên tử; key room-hold:{roomId}; không gia hạn/ghi đè.
- [ ] requireValid phân biệt HOLD_EXPIRED với INVALID_HOLD; Redis lỗi → HOLD_SERVICE_UNAVAILABLE (503).
  Token sai chủ khi tạo booking là INVALID_HOLD; release sai chủ còn tồn tại là ACCESS_DENIED (403).
- [ ] Chốt token → roomId khi release chỉ có token trong URL (locator + random secret hoặc token index TTL).
  Kiểm tra chủ và compare-delete an toàn khi TTL hết giữa chừng; không GET rồi DEL.
- [ ] M2 giữ khóa Room trong acquire/verify. Sau booking commit, cleanup compare-delete best effort;
  lỗi cleanup không trả booking thất bại. Acquire thành công nhưng MySQL rollback thì cleanup đúng token best effort.
- [ ] Gửi phiên bản/tag hoặc digest MySQL/Redis đã chốt, lock timeout và loại exception thực tế khi lock timeout/deadlock.
  Không dùng latest để nghiệm thu lặp lại; chi tiết quyết định ở [m2-decisions.md](m2-decisions.md).

## P1 — sau khi đặt phòng chạy được

- [ ] Query occupied CHECKED_IN theo roomId + danh sách housekeeping không PII.
- [ ] HotelService/ServiceOrder/Item Entity, Repository; khóa Booking cha trước tạo/hủy, query ORDERED với item snapshot.
- [ ] Invoice/Item Entity, Repository, UNIQUE(booking_id); lookup invoice theo booking và theo chủ.
- [ ] M3/M4 fixture MySQL + Redis; M2 tích hợp Controller → Service → Repository thật, không thêm tầng để thay thế M3.

## Tiêu chí nhận bàn giao P0

- [ ] Search/count query đúng biên ngày; cùng ngày trả/nhận không giao nhau.
- [ ] Một luồng guest và một luồng FRONT_DESK dùng phiên + CSRF thật, hold 15 phút, snapshot giá đúng.
- [ ] Hai connection đặt cùng phòng không trùng; Redis lỗi không bypass hold; TTL hết trong transaction vẫn được khóa MySQL bảo vệ.
- [ ] Sai chủ trả 404 với booking; release không xóa nhầm hold mới; afterCommit cleanup lỗi không đổi phản hồi thành thất bại.

Các mục trên chưa được chứng minh bằng test độc lập hiện tại; cần code M3 và integration test.
