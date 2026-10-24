# Hotel Reservation & Guest Service Management Platform

## 1. Giới thiệu

Hệ thống quản lý đặt phòng và dịch vụ khách sạn (Hotel Reservation & Guest Service Management Platform) là dự án hỗ trợ quản lý các hoạt động đặt phòng, lưu trú, sử dụng dịch vụ và thanh toán tại khách sạn.

Các chức năng dự kiến:
- Tìm kiếm phòng còn trống.
- Đặt phòng theo ngày nhận và trả phòng.
- Quản lý thông tin đặt phòng.
- Đặt và quản lý dịch vụ phòng.
- Theo dõi trạng thái dọn phòng.
- Quản lý hóa đơn và trả phòng.

## 2. Thành viên nhóm

Nhóm gồm 4 thành viên:

- **Thành viên 1 (Phommasone) – Frontend:** Phụ trách giao diện người dùng.
- **Thành viên 2 (SU) – Backend:** Phụ trách xử lý các chức năng nghiệp vụ.
- **Thành viên 3 (Tu) – Nhóm trưởng:** Phụ trách quản lý repository, tổ chức nhánh, kiểm tra và gộp Pull Request.
- **Thành viên 4 (Shinn thant) – Testing:** Phụ trách kiểm thử và đánh giá hệ thống.


## 3. Công nghệ sử dụng

- **Frontend:** HTML, CSS, JavaScript.
- **Backend:** Java, Spring Boot .
- **Database:** PostgreSQL .
- **Testing:** JUnit.

## 4. Cấu trúc dự án

```text
Hotel-Management/
├── README.md
├── .gitignore
├── docs/
│   ├── architecture.md
│   ├── database-design.md
│   └── ui-design.md
├── backend/
└── frontend/
```

## 5. Hướng dẫn cài đặt

**Yêu cầu:**
- Git.
- Tài khoản GitHub.
- Visual Studio Code hoặc trình soạn thảo văn bản.

Clone repository về máy:

```bash
git clone https://github.com/dothanhtu2003/Hotel-Management.git
```

Di chuyển vào thư mục dự án:

```bash
cd Hotel-Management
```

## 6. Hướng dẫn chạy


Kiểm tra nhánh hiện tại:

```bash
git branch
```

Cập nhật phiên bản mới nhất:

```bash
git pull origin main
```

Tạo nhánh mới:

```bash
git switch -c feature/frontend-setup
```

Thêm và lưu thay đổi:

```bash
git add .
git commit -m "Setup project structure"
```

Đẩy nhánh lên GitHub:

```bash
git push -u origin feature/frontend-setup
```

Sau đó tạo Pull Request trên GitHub để đề nghị gộp thay đổi vào nhánh chính.

## 7. Kiểm thử

Các nội dung kiểm tra:
- Các thành viên tạo nhánh riêng thành công.
- Các file và thư mục được quản lý bằng Git.
- Commit và push thành công lên GitHub.
- Tạo Pull Request thành công.
- Xử lý xung đột khi gộp nhánh.
- Gộp các nhánh vào `main` thành công.
- Các thành viên cập nhật được phiên bản mới nhất bằng `git pull`.


---

**Repository:** https://github.com/dothanhtu2003/Hotel-Management

**Mục đích:** Thực hành quy trình quản lý mã nguồn và làm việc nhóm bằng Git/GitHub trong Lab 07.