# Playwright Java TestNG Framework

[![CI](https://github.com/YOUR_USERNAME/playwright-java-testng/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/playwright-java-testng/actions/workflows/ci.yml)
[![Allure Report](https://img.shields.io/badge/Allure-Report-orange)](https://YOUR_USERNAME.github.io/playwright-java-testng/)
[![Java](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/projects/jdk/17/)
[![Playwright](https://img.shields.io/badge/Playwright-1.44-green)](https://playwright.dev/java/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10-red)](https://testng.org/)

Enterprise-grade test automation framework combining **Playwright**, **Java 17**, **TestNG**, and **Allure Reporting** — designed for scalability, CI/CD integration, and parallel execution.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Browser Automation | Playwright 1.44 |
| Test Runner | TestNG 7.10 |
| API Testing | REST Assured 5.4 |
| Test Data | datafaker 2.2 |
| Reporting | Allure 2.27 |
| Assertions | AssertJ |
| Config Management | OWNER |
| Build | Maven 3.9 |
| CI/CD | GitHub Actions |
| Java | 17 (LTS) |

---

## Project Structure

```
src/
├── main/java/com/framework/
│   ├── config/          # FrameworkConfig (OWNER), ConfigManager
│   ├── pages/           # BasePage + Page Objects (POM)
│   ├── models/          # Request/response DTOs (Lombok)
│   ├── components/      # Reusable UI components
│   └── utils/           # BrowserManager, ScreenshotUtils, RetryAnalyzer,
│                        #   TestDataFactory (datafaker), JsonUtils
│
└── test/java/com/framework/
    ├── tests/
    │   ├── ui/          # LoginTest, InventoryTest, ...
    │   └── api/         # UserApiTest, ...
    ├── hooks/           # TestListener, RetryListener
    └── resources/
        ├── logback-test.xml
        └── suites/      # smoke.xml, regression.xml, api.xml
```

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+

### Run smoke suite
```bash
mvn test -Psmoke
```

### Run full regression
```bash
mvn test -Pregression
```

### Run with specific browser
```bash
mvn test -Psmoke -Dbrowser=firefox -Dheadless=false
```

### Run API tests only
```bash
mvn test -Papi
```

### Generate and open Allure report
```bash
mvn allure:serve
```

---

## Configuration

All settings are in `src/main/resources/config.properties`.  
Every value can be overridden via system property or environment variable:

```bash
mvn test -Psmoke -Dbrowser=webkit -Dheadless=false -Ddefault.timeout=60000
```

| Property | Default | Description |
|---|---|---|
| `base.url` | saucedemo.com | Target application URL |
| `browser` | chromium | chromium / firefox / webkit |
| `headless` | true | Run browser headless |
| `default.timeout` | 30000 | Element wait timeout (ms) |
| `screenshot.on.failure` | true | Capture screenshot on test failure |
| `trace.on.failure` | true | Capture Playwright trace on failure |
| `retry.count` | 1 | Retry count for flaky tests |
| `parallel.threads` | 4 | TestNG parallel thread count |

---

## CI/CD Pipeline

The GitHub Actions pipeline (`/.github/workflows/ci.yml`) provides:

- **On push/PR** → Smoke suite (chromium, headless)
- **Manual trigger** → Choose suite, browser, headless mode
- **Nightly (02:00 UTC)** → Full regression on chromium + firefox + webkit in parallel
- **Allure report** → Auto-published to GitHub Pages after every main branch run
- **PR comment** → Automatic report link posted on every pull request

### Secrets (optional)
| Secret | Description |
|---|---|
| `BASE_URL` | Override target URL per environment |
| `API_BASE_URL` | Override API base URL |

---

## Key Design Decisions

### Thread-safe browser management
`BrowserManager` uses `ThreadLocal` for the full Playwright → Browser → BrowserContext → Page stack. Safe for TestNG `parallel="methods"` or `parallel="classes"`.

### Page Object Model
`BasePage` provides a clean abstraction over raw Playwright selectors. All interactions are logged and wrapped as Allure steps. Concrete page objects expose a fluent, business-readable API.

### Selector strategy (priority order)
1. `data-test` attributes (most stable)
2. ARIA roles (`getByRole`)
3. Labels (`getByLabel`)
4. Text (`getByText`)
5. CSS selectors (last resort)

### Failure artifacts
On test failure: screenshot + Playwright trace (`.zip`) are captured automatically and uploaded as GitHub Actions artifacts. Traces can be inspected at `trace.playwright.dev`.

### Retry & flaky-test handling
`RetryAnalyzer` is applied to every `@Test` automatically via `RetryListener` (an `IAnnotationTransformer`) — no per-test annotation needed. Retry count is config-driven (`retry.count`).

### Test data
`TestDataFactory` (datafaker, German locale) generates randomized realistic data. API request bodies are built from typed Lombok DTOs in `models/` and serialized via `JsonUtils` — no brittle hand-written JSON strings.

---

## Extending the Framework

### Add a new page object
```java
public class CheckoutPage extends BasePage {
    private static final String CONTINUE_BTN = "[data-test='continue']";

    @Step("Click continue")
    public CheckoutPage clickContinue() {
        click(CONTINUE_BTN);
        return this;
    }

    @Override
    public boolean isLoaded() {
        return getCurrentUrl().contains("checkout");
    }
}
```

### Add a new test
```java
@Epic("Checkout")
@Feature("Order Completion")
public class CheckoutTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    public void userCanCompleteCheckout() {
        // arrange → act → assert
    }
}
```

---

## Author

**Eyed Friaa** — Test Automation Engineer (AI-assisted)  
Java · Playwright · TestNG · REST Assured · CI/CD  
Köln, Germany · open to remote & freelance

[GitHub](https://github.com/Eyed-Friaa)· eyedfriaa@yahoo.com
