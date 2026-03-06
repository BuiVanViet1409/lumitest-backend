# LumiTest - Nền tảng Kiểm thử Tự động (Phiên bản MongoDB)

## 1. Tổng quan hệ thống

LumiTest là một nền tảng tự động hóa QA dựa trên web, cho phép các kỹ sư QA tạo và thực thi các kịch bản kiểm thử (test cases) mà không cần viết mã. Hệ thống sử dụng Playwright để tự động hóa trình duyệt và MongoDB để lưu trữ các định nghĩa kiểm thử và kết quả thực thi.

## 2. Yêu cầu hệ thống

- **Java 17**
- **Maven**
- **MongoDB** (Bản cục bộ hoặc trên Cloud)
- **Node.js** (Bắt buộc để cài đặt trình duyệt Playwright)

## 3. Cài đặt MongoDB

Nếu bạn chưa cài đặt MongoDB, bạn có thể tải xuống từ [MongoDB Community Server](https://www.mongodb.com/try/download/community).
Khởi động MongoDB bằng lệnh:

```bash
mongod
```

## 4. Cài đặt NodeJS & Trình duyệt Playwright

Để cài đặt các tệp thực thi trình duyệt cần thiết cho Playwright:

```bash
# Cài đặt NodeJS từ nodejs.org
# Sau đó chạy lệnh:
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

## 5. Chạy Spring Boot

```bash
# Cài đặt các phụ thuộc
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

Ứng dụng sẽ bắt đầu tại `http://localhost:8080`.

## 6. Thực thi Kiểm thử

1. Tạo một Test Case thông qua API hoặc giao diện người dùng (UI).
2. Kích hoạt thực thi bằng cách sử dụng endpoint `run`.
3. Ảnh chụp màn hình sẽ được lưu trong thư mục `src/main/resources/static/screenshots/{executionId}/`.

## 7. Ví dụ các yêu cầu API

### Tạo Test Case

`POST /api/testcases`

```json
{
  "name": "Kiểm tra Đăng nhập",
  "applicationUrl": "https://example.com/login",
  "steps": [
    {
      "order": 1,
      "action": "OPEN_URL",
      "value": "https://example.com/login"
    },
    {
      "order": 2,
      "action": "INPUT_TEXT",
      "selector": "#username",
      "value": "admin"
    }
  ]
}
```

### Chạy Test Case

`POST /api/executions/run/{testCaseId}`

### Lấy Báo cáo

`GET /api/executions/{executionId}/report`
