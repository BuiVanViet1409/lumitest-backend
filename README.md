# LumiTest Backend - Automated Testing Tool

Phần Backend của hệ thống LumiTest, được xây dựng bằng Spring Boot và Playwright.

## 1. Yêu cầu hệ thống

- **Java 17**
- **Maven 3.8+**
- **PostgreSQL 14+**

## 2. Cấu hình (YAML)

Project sử dụng file `src/main/resources/application.yml`. Bạn hãy cập nhật thông tin database tại đây:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lumitest
    username: your_username
    password: your_password
```

## 3. Cài đặt ban đầu

Sau khi clone project, bạn cần cài đặt Playwright và các trình duyệt:

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

## 4. Cách chạy

```bash
mvn spring-boot:run
```

API sẽ lắng nghe tại: `http://localhost:8080`

## 5. Chức năng chính

- Quản lý Test Cases & Test Steps.
- Tự động thực thi test qua Playwright.
- Tự động chụp ảnh màn hình mỗi bước (`src/main/resources/static/screenshots`).
- API cung cấp dữ liệu cho Frontend React.
