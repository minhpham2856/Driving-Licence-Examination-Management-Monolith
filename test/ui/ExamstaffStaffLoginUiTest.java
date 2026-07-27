//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * UI smoke: staff login page renders and form fields are present.
// * Requires Tomcat running at {@link SeleniumUiTestBase#baseUrl()}.
// */
//public class ExamstaffStaffLoginUiTest extends SeleniumUiTestBase {
//
//    private WebDriver driver;
//
//    @Before
//    public void setUp() {
//        driver = newChromeDriver();
//    }
//
//    @After
//    public void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
//
//    @Test
//    public void staffLoginPageShowsIdentifierAndPasswordFields() {
//        driver.get(url("/views/auth/internal/login.jsp"));
//
//        WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identifier")));
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
//
//        assertTrue(driver.findElement(By.cssSelector("form.staff-login__form")).isDisplayed());
//        assertTrue(driver.getTitle() != null && !driver.getTitle().isBlank());
//    }
//}
