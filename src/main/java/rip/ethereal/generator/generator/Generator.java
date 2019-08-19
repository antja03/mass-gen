package rip.ethereal.generator.generator;

import org.openqa.selenium.*;
import rip.ethereal.generator.AccountGenerator;
import rip.ethereal.generator.config.enums.LogDensity;
import rip.ethereal.generator.util.DriverUtils;
import rip.ethereal.generator.util.KillableThread;

import java.io.File;
import java.util.Optional;

/**
 * @author antja03
 */
public abstract class Generator extends KillableThread {

    /**
     * The url of the website we're trying to make accounts for
     */
    private final String pageUrl;

    /**
     * The account information of the last generated account-
     */
    private String email;
    private String username;
    private String password;

    public Generator(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    /**
     * Runs generator setup and starts the generation loop
     */
    @Override
    public void run() {
        DriverUtils.disableSeleniumLogging();
        startThread();

        while (isRunning()) {
            generate();
            sleep(5000);
        }
    }

    /**
     * Generates an account
     *
     * @return Whether the account was successfully generated or not
     */
    protected abstract void generate();

    /**
     * @return The file where each generated account should be written to
     */
    protected abstract File getAccountsFile();

    protected void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected Object executeScript(WebDriver driver, String script) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        return executor.executeScript(script);
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    //    protected Optional<WebElement> findElement(WebDriver driver, By by, int maxAttempts) {
//        WebElement element = null;
//
//        int attempts = 0;
//        while (element == null && attempts < maxAttempts) {
//            try {
//                element = driver.findElement(by);
//                if (AccountGenerator.config.getLogDensity().equals(LogDensity.ALL))
//                    AccountGenerator.LOGGER.warn("Found the element:" + by.toString());
//            } catch (NoSuchElementException e) {
//                if (AccountGenerator.config.getLogDensity().equals(LogDensity.ALL)) {
//                    AccountGenerator.LOGGER.warn("Failed to find the element:" + by.toString());
//                    AccountGenerator.LOGGER.warn("Retrying in 1 second...");
//                }
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
//            }
//            attempts++;
//        }
//
//        return Optional.ofNullable(element);
//    }

}
