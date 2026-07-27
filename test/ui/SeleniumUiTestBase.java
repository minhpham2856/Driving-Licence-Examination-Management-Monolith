//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//
//import java.time.Duration;
//
///**
// * Shared Selenium WebDriver setup for UI smoke tests.
// * Base URL: system property {@code test.base.url} or {@code TEST_BASE_URL} env var.
// */
//abstract class SeleniumUiTestBase {
//
//    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
//
//    static String baseUrl() {
//        String fromProperty = System.getProperty("test.base.url");
//        if (fromProperty != null && !fromProperty.isBlank()) {
//            return trimTrailingSlash(fromProperty.trim());
//        }
//        String fromEnv = System.getenv("TEST_BASE_URL");
//        if (fromEnv != null && !fromEnv.isBlank()) {
//            return trimTrailingSlash(fromEnv.trim());
//        }
//        return "http://localhost:8080/Driving-Licence-Examination-Management-Monolith";
//    }
//
//    static WebDriver newChromeDriver() {
//        ChromeOptions options = new ChromeOptions();
//        if (Boolean.parseBoolean(System.getProperty("test.headless", "true"))) {
//            options.addArguments("--headless=new");
//        }
//        options.addArguments("--window-size=1400,900");
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");
//        WebDriver driver = new ChromeDriver(options);
//        driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
//        return driver;
//    }
//
//    static String url(String path) {
//        if (path == null || path.isBlank()) {
//            return baseUrl();
//        }
//        return path.startsWith("/") ? baseUrl() + path : baseUrl() + "/" + path;
//    }
//
//    private static String trimTrailingSlash(String value) {
//        if (value.endsWith("/")) {
//            return value.substring(0, value.length() - 1);
//        }
//        return value;
//    }
//}
