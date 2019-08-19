package rip.ethereal.generator.util;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import rip.ethereal.generator.AccountGenerator;
import rip.ethereal.generator.config.enums.ProxyMode;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author antja03
 * @since 8/8/2019
 */
public class DriverUtils {

    public static ChromeDriver createGeneratorDriver(boolean disableAllLogging, boolean useProxy) {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("silent");

        if (!AccountGenerator.config.shouldShowBrowsers())
            chromeOptions.addArguments("headless");

        if (useProxy) {
           if (!AccountGenerator.config.getProxyMode().equals(ProxyMode.NONE))
               chromeOptions.addArguments("--proxy-server=" + AccountGenerator.getProxy());
        }

        if (!disableAllLogging) {
            LoggingPreferences loggingPreferences = new LoggingPreferences();
            loggingPreferences.enable(LogType.BROWSER, Level.OFF);
            loggingPreferences.enable(LogType.SERVER, Level.OFF);
            loggingPreferences.enable(LogType.DRIVER, Level.OFF);
            loggingPreferences.enable(LogType.PROFILER, Level.OFF);
            loggingPreferences.enable(LogType.CLIENT, Level.OFF);
            chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
        }

        return new ChromeDriver(chromeOptions);
    }

    public static void waitUntilElementExists(WebDriver driver, By by) {
        final FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(60))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class);
        fluentWait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public static void waitUntil(WebDriver driver, ExpectedCondition condition) {
        final FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(60))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class);
        fluentWait.until(condition);
    }

    public static void waitUntilPageLoaded(WebDriver driver) {
        final FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(60))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class);
        fluentWait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    public static void waitUntilCaptchaExists(WebDriver driver) {
        final FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(60))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class);
        fluentWait.until(ExpectedConditions.jsReturnsValue(CaptchaUtils.getSiteKeyScript()));
    }

    public static void disableSeleniumLogging() {
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
    }

}
