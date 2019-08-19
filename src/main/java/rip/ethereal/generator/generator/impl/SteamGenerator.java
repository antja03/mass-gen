package rip.ethereal.generator.generator.impl;

import org.openqa.selenium.*;
import rip.ethereal.generator.AccountGenerator;
import rip.ethereal.generator.generator.Generator;
import rip.ethereal.generator.util.CaptchaUtils;
import rip.ethereal.generator.util.DriverUtils;
import rip.ethereal.generator.util.Log;
import java.io.File;
import java.io.IOException;

/**
 * @author antja03
 * @since 8/6/2019
 */
public class SteamGenerator extends Generator {

    public SteamGenerator() {
        super("https://store.steampowered.com/join");
    }

    @Override
    public void generate() {
        setUsername(AccountGenerator.generateUsername());
        setEmail(String.format("%s@%s", getUsername(), AccountGenerator.getRandomDomain()));
        setPassword(AccountGenerator.generatePassword());

        WebDriver driver = DriverUtils.createGeneratorDriver(true, true);
        driver.get(getPageUrl());
        
        DriverUtils.waitUntilPageLoaded(driver);

        if (!fillOutForm(driver)) {
            driver.quit();
            driver.close();
            generate();
            return;
        }

        if (!solveCaptchaAndSubmit(driver)) {
            driver.quit();
            driver.close();
            generate();
            return;
        }

        if (checkErrors(driver, 10, 1000)) {
            Log.error("An error has occurred (most likely a proxy issue)");
            Log.error("This thread will restart");
            driver.quit();
            driver.close();
            generate();
            return;
        }


        if (!completeAccount(driver)) {
            driver.quit();
            driver.close();
            generate();
            return;
        }

        if (checkErrors(driver, 10, 1000)) {
            Log.error("An error has occurred (most likely a proxy issue)");
            Log.error("This thread will restart");
            driver.quit();
            driver.close();
            generate();
            return;
        }

        attemptDisableSteamguard(driver);
        sleep(1000);

        driver.quit();
        driver.close();

        StringBuilder comboBuilder = new StringBuilder();
        comboBuilder.append(getUsername());
        comboBuilder.append(":");
        comboBuilder.append(getPassword());
        Log.info("Discord account generated: " + comboBuilder.toString());

        File accountsFile = getAccountsFile();
        AccountGenerator.accountWriter.addToQueue(accountsFile, comboBuilder.toString());
    }

    /**
     * @return The file where accounts from this generator type will be saved to
     */
    @Override
    public File getAccountsFile() {
        File accountsFile =  new File(AccountGenerator.WORKING_DIRECTORY + File.separator + "steam_accounts.txt");

        int attempts = 0;
        while (!accountsFile.exists() && attempts < 6) {
            Log.info("Accounts file not found... creating it now");
            try {
                accountsFile.createNewFile();
                Log.info("Accounts file created");
            } catch (IOException e) {
                Log.error("Could not create 'steam_accounts.txt' please make it manually");
                Log.error("Retrying in 10 seconds");
                attempts++;
                sleep(10000);
            }
        }

        if (!accountsFile.exists()) {
            kill();
        }

        return accountsFile;
    }

    private boolean fillOutForm(WebDriver driver) {
        DriverUtils.waitUntilPageLoaded(driver);
        DriverUtils.waitUntilElementExists(driver, By.id("email"));
        DriverUtils.waitUntilElementExists(driver, By.id("reenter_email"));
        DriverUtils.waitUntilElementExists(driver, By.id("i_agree_check"));

        WebElement emailField, verifyEmailField, agreeCheckbox;

        try {
            emailField = driver.findElement(By.id("email"));
            verifyEmailField = driver.findElement(By.id("reenter_email"));
            agreeCheckbox = driver.findElement(By.id("i_agree_check"));
        } catch (NoSuchElementException e) {
            Log.error("Couldn't find the required elements in the form. This thread will restart");
            return false;
        }

        Log.info("Filling out all fields");
        emailField.sendKeys(getEmail());
        verifyEmailField.sendKeys(getEmail());
        agreeCheckbox.click();
        return true;
    }

    private boolean solveCaptchaAndSubmit(WebDriver driver) {
        DriverUtils.waitUntilPageLoaded(driver);
        DriverUtils.waitUntilElementExists(driver, By.id("g-recaptcha-response"));

        WebElement captchaResponseField;

        try {
             captchaResponseField = driver.findElement(By.id("g-recaptcha-response"));
        } catch (NoSuchElementException e) {
            Log.error("Couldn't find the required elements in the form. This thread will restart");
            return false;
        }

        Log.info("Attempting to solve the captcha");
        final String siteKey = (String) executeScript(driver, CaptchaUtils.getSiteKeyScript());
        final String captchaToken = CaptchaUtils.solveCaptcha(siteKey, getPageUrl());

        if (captchaToken.isEmpty()) {
            Log.warn("2captcha returned an empty captcha token. This thread will restart");
            return false;
        }

        Log.info("Filling out the response field");
        executeScript(driver, "document.getElementById('g-recaptcha-response').style.display='block';");
        captchaResponseField.sendKeys(captchaToken);
        sleep(10000);

        Log.info("Submitting the form");
        executeScript(driver, "FinishFormVerification(true);");
        return true;
    }

    private boolean completeAccount(WebDriver webDriver) {
        DriverUtils.waitUntilPageLoaded(webDriver);
        DriverUtils.waitUntilElementExists(webDriver, By.id("accountname"));
        DriverUtils.waitUntilElementExists(webDriver, By.id("password"));
        DriverUtils.waitUntilElementExists(webDriver, By.id("reenter_password"));

        WebElement nameElement, passwordElement, repasswordElement;

        try {
            nameElement = webDriver.findElement(By.id("accountname"));
            passwordElement = webDriver.findElement(By.id("password"));
            repasswordElement = webDriver.findElement(By.id("reenter_password"));
        } catch (NoSuchElementException e) {
            Log.error("Couldn't find the required elements in the form. This thread will restart");
            return false;
        }

        nameElement.sendKeys(getUsername());
        passwordElement.sendKeys(getPassword());
        repasswordElement.sendKeys(getPassword());

        sleep(2500);
        executeScript(webDriver, "CompleteCreateAccount();");
        return true;
    }

    private boolean attemptDisableSteamguard(WebDriver webDriver) {
        webDriver.navigate().to("https://store.steampowered.com/twofactor/manage_action");
        DriverUtils.waitUntilPageLoaded(webDriver);
        DriverUtils.waitUntilElementExists(webDriver, By.id("none_authenticator_check"));

        WebElement noneElement;

        try {
            noneElement = webDriver.findElement(By.id("none_authenticator_check"));
        } catch (NoSuchElementException e) {
            Log.error("Couldn't find the required elements in the form. This thread will restart");
            return false;
        }

        noneElement.click();
        sleep(1000);
        executeScript(webDriver, "document.getElementById('none_authenticator_form').submit();");
        return true;
    }

    private boolean checkErrors(WebDriver driver, int count, int delay) {
        int attempts = 0;
        while (attempts < count) {
            try {
                WebElement errorElement = driver.findElement(By.xpath("//div[@id='error_display' and @style='background-image: none; background-color: rgba(0, 0, 0, 0.5);']"));
                return true;
            } catch (NoSuchElementException e) {
                // Silent
            }

            sleep(delay);
            attempts++;
        }

        return false;
    }

}
