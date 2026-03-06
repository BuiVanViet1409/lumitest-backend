# LumiTest - Automated Testing Platform (MongoDB Edition)

## 1. System Overview

LumiTest is a web-based QA automation platform that allows QA engineers to create and execute test cases without writing code. It uses Playwright for browser automation and MongoDB for persistent storage of test definitions and execution results.

## 2. Prerequisites

- **Java 17**
- **Maven**
- **MongoDB** (Local or Remote)
- **Node.js** (Required for Playwright browsers)

## 3. Install MongoDB

If you don't have MongoDB installed, you can download it from [MongoDB Community Server](https://www.mongodb.com/try/download/community).
Start MongoDB with:

```bash
mongod
```

## 4. Install NodeJS & Playwright Browsers

To install the necessary browser binaries for Playwright:

```bash
# Install NodeJS from nodejs.org
# Then run:
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

## 5. Run Spring Boot

```bash
# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start at `http://localhost:8080`.

## 6. Run Test Execution

1. Create a Test Case via API or UI.
2. Trigger the execution using the `run` endpoint.
3. Screenshots will be saved in `/screenshots/{executionId}/`.

## 7. Example API Requests

### Create Test Case

`POST /api/testcases`

```json
{
  "name": "Login Test",
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

### Run Test Case

`POST /api/executions/run/{testCaseId}`

### Get Report

`GET /api/executions/{executionId}/report`
