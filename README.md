# 🤖 AI Chatbot Automation Testing Framework

A production-ready, enterprise-grade automation testing framework for AI-powered chatbot systems. Built with Java, Selenium, TestNG, REST Assured, and Allure Reports.

---

## 📋 Table of Contents
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Test Categories](#test-categories)
- [Reporting](#reporting)
- [CI/CD Integration](#cicd-integration)
- [Performance Testing with JMeter](#performance-testing-with-jmeter)
- [API Testing with Postman](#api-testing-with-postman)

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              AI Chatbot Testing Framework                │
├──────────────────────┬──────────────────────────────────┤
│    UI Layer          │         API Layer                 │
│  (Selenium + POM)    │      (REST Assured)               │
├──────────────────────┴──────────────────────────────────┤
│                  Core Utilities                          │
│  DriverManager │ ApiUtil │ WaitUtil │ ScreenshotUtil     │
│  TestDataUtil  │ ExtentReportManager │ ConfigManager     │
├─────────────────────────────────────────────────────────┤
│                  Test Suites                             │
│  UI Tests │ API Tests │ Functional │ Negative │ Security │
│  Performance Tests                                       │
├─────────────────────────────────────────────────────────┤
│              CI/CD & Reporting                           │
│  GitHub Actions │ Jenkins │ Allure │ Extent Reports      │
└─────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Category         | Tool / Library              | Version  |
|------------------|-----------------------------|----------|
| Language         | Java                        | 11       |
| Build Tool       | Apache Maven                | 3.9+     |
| UI Automation    | Selenium WebDriver          | 4.18.1   |
| Driver Mgmt      | WebDriverManager            | 5.7.0    |
| API Testing      | REST Assured                | 5.4.0    |
| Test Framework   | TestNG                      | 7.9.0    |
| Reporting        | Allure Reports              | 2.25.0   |
| Reporting        | ExtentReports               | 5.1.1    |
| Logging          | Log4j2                      | 2.22.1   |
| Test Data        | Java Faker                  | 1.0.2    |
| JSON             | Jackson Databind            | 2.16.1   |
| Load Testing     | Apache JMeter               | 5.6.3    |
| API Testing      | Postman / Newman            | Latest   |
| CI/CD            | GitHub Actions / Jenkins    | —        |

---

## Project Structure

```
chatbot-testing-framework/
├── pom.xml                                    # Maven dependencies
├── testng.xml                                 # TestNG suite configuration
├── Jenkinsfile                                # Jenkins pipeline
├── .github/
│   └── workflows/
│       └── chatbot-test-pipeline.yml          # GitHub Actions pipeline
├── jmeter/
│   └── chatbot-load-test.jmx                 # JMeter load test plan
├── postman/
│   └── chatbot-api-collection.json           # Postman collection
├── src/
│   ├── main/
│   │   ├── java/com/chatbot/
│   │   │   ├── config/
│   │   │   │   └── ConfigManager.java        # Configuration loader
│   │   │   ├── pages/
│   │   │   │   └── ChatBotPage.java          # Page Object Model
│   │   │   └── utils/
│   │   │       ├── DriverManager.java        # WebDriver factory
│   │   │       ├── ApiUtil.java              # REST Assured wrapper
│   │   │       ├── WaitUtil.java             # Explicit wait helpers
│   │   │       ├── ScreenshotUtil.java       # Screenshot capture
│   │   │       ├── TestDataUtil.java         # Test data & generators
│   │   │       └── ExtentReportManager.java  # Extent Reports
│   │   └── resources/
│   │       ├── config.properties             # Framework configuration
│   │       └── log4j2.xml                    # Logging configuration
│   └── test/
│       ├── java/com/chatbot/
│       │   ├── ui/
│       │   │   ├── BaseTest.java             # TestNG base class
│       │   │   ├── TestListener.java         # TestNG listener
│       │   │   └── ChatBotUITest.java        # UI test suite
│       │   ├── api/
│       │   │   └── ChatBotApiTest.java       # API test suite
│       │   ├── functional/
│       │   │   └── FunctionalTest.java       # Functional tests
│       │   ├── negative/
│       │   │   └── NegativeTest.java         # Negative tests
│       │   ├── security/
│       │   │   └── SecurityTest.java         # Security tests
│       │   └── performance/
│       │       └── PerformanceTest.java      # Performance tests
│       └── resources/
│           ├── schemas/
│           │   └── chat-response-schema.json # JSON schema
│           └── testdata/
│               ├── chat-prompts.json         # Test prompts
│               └── jmeter-messages.csv       # JMeter CSV data
```

---

## Setup & Installation

### Prerequisites

- Java JDK 11+
- Maven 3.9+
- Google Chrome / Firefox / Edge
- Git

### Clone & Install

```bash
git clone https://github.com/your-org/chatbot-testing-framework.git
cd chatbot-testing-framework
mvn clean install -DskipTests
```

---

## Configuration

Edit `src/main/resources/config.properties`:

```properties
app.base.url=https://your-chatbot-app.com
api.base.url=https://api.your-chatbot.com/v1
api.valid.token=Bearer your-jwt-token-here
browser=chrome
browser.headless=false
```

You can also override any property via Maven system properties:

```bash
mvn test -Dapp.base.url=https://staging.chatbot.com -Dbrowser=firefox
```

---

## Running Tests

### All Tests

```bash
mvn test
```

### Specific Group

```bash
mvn test -Dgroups=smoke
mvn test -Dgroups=functional,api
mvn test -Dgroups=security
mvn test -Dgroups=performance
```

### Headless Browser

```bash
mvn test -Dbrowser.headless=true
```

### Parallel Execution

Parallelism is configured in `testng.xml` (`parallel="classes" thread-count="4"`).

### Generate Allure Report

```bash
mvn allure:serve          # Opens in browser
mvn allure:report         # Generates static report
```

---

## Test Categories

| Group         | Class                  | Coverage                                      |
|---------------|------------------------|-----------------------------------------------|
| `smoke`       | ChatBotUITest, ApiTest | Page load, health check, basic chat           |
| `functional`  | FunctionalTest         | Greetings, FAQs, session continuity, tone     |
| `api`         | ChatBotApiTest         | Status codes, schema, response time, methods  |
| `negative`    | NegativeTest           | Special chars, SQL strings, long input, emoji |
| `security`    | SecurityTest           | XSS, SQL injection, auth tokens, data leakage |
| `performance` | PerformanceTest        | 10/25/50 concurrent users, spike, sustained   |
| `ui`          | ChatBotUITest          | Browser UI interactions, conversation flow    |

---

## Reporting

### Allure Reports
After test execution:

```bash
mvn allure:serve
```

Report includes: pass/fail status, execution time, screenshots on failure, step-by-step trace.

### Extent Reports
HTML report auto-generated at: `reports/ExtentReport.html`

### Screenshots
Captured automatically on test failure: `screenshots/<TestName>_<timestamp>.png`

### Logs
- All logs: `logs/chatbot-tests.log`
- Errors only: `logs/chatbot-errors.log`

---

## CI/CD Integration

### GitHub Actions

Pipeline defined in `.github/workflows/chatbot-test-pipeline.yml`.

Required GitHub Secrets:
```
APP_BASE_URL       — Chatbot web URL
API_BASE_URL       — API base URL
API_VALID_TOKEN    — Valid JWT token
SLACK_WEBHOOK_URL  — Slack notification (optional)
```

Triggers:
- Push to `main`, `develop`
- Pull requests to `main`
- Nightly schedule (02:00 UTC)
- Manual dispatch with parameter selection

### Jenkins

Pipeline defined in `Jenkinsfile`.

Required Jenkins Credentials:
```
CHATBOT_APP_URL    — App URL secret
CHATBOT_API_URL    — API URL secret
CHATBOT_API_TOKEN  — API token secret
```

Required Jenkins Plugins:
- Maven Integration
- Allure Jenkins Plugin
- HTML Publisher
- Email Extension

---

## Performance Testing with JMeter

```bash
cd jmeter/
jmeter -n \
  -t chatbot-load-test.jmx \
  -l results/result.jtl \
  -e -o results/html-report \
  -JBASE_URL=https://api.your-chatbot.com/v1 \
  -JAUTH_TOKEN="Bearer your-token"
```

Scenarios included:
- **Smoke Load** — 5 users / 30 seconds
- **Normal Load** — 25 users / 60 seconds with 10s ramp
- **Stress Load** — 100 users / 120 seconds (configure in JMX)
- **Spike Test** — sudden 50-user burst

---

## API Testing with Postman

Import `postman/chatbot-api-collection.json` into Postman, or run headlessly with Newman:

```bash
# Install Newman
npm install -g newman newman-reporter-allure

# Run collection
newman run postman/chatbot-api-collection.json \
  --env-var "baseUrl=https://api.your-chatbot.com/v1" \
  --env-var "authToken=Bearer your-token" \
  --reporters cli,allure \
  --reporter-allure-export allure-results
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-test-suite`
3. Add tests following existing patterns
4. Run `mvn test -Dgroups=smoke` to verify
5. Submit a pull request

---

## License

MIT License — see LICENSE file for details.
