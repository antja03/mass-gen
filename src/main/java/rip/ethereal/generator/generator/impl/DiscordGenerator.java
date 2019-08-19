package rip.ethereal.generator.generator.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import rip.ethereal.generator.AccountGenerator;
import rip.ethereal.generator.generator.Generator;
import rip.ethereal.generator.util.CaptchaUtils;
import rip.ethereal.generator.util.DriverUtils;
import rip.ethereal.generator.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author antja03
 * @since 8/8/2019
 */
public class DiscordGenerator extends Generator {

    public DiscordGenerator() {
        super("https://discordapp.com/register");
    }

    /**
     * Generates a discord account
     *
     * @return Whether the account was successfully generated or not
     */
    @Override
    protected void generate() {
        setUsername(AccountGenerator.generateUsername());
        setEmail(String.format("%s@%s", getUsername(), AccountGenerator.getRandomDomain()));
        setPassword(AccountGenerator.generatePassword());

        WebDriver driver = DriverUtils.createGeneratorDriver(true, true);
        driver.get(getPageUrl());

        DriverUtils.waitUntilPageLoaded(driver);
        if (fillOutFirstForm(driver) && solveCaptcha(driver)) {
            StringBuilder comboBuilder = new StringBuilder();
            comboBuilder.append(getEmail());
            comboBuilder.append(":");
            comboBuilder.append(getUsername());
            comboBuilder.append(":");
            comboBuilder.append(getPassword());
            Log.info("Discord account generated: " + comboBuilder.toString());

            File accountsFile = getAccountsFile();
            AccountGenerator.accountWriter.addToQueue(accountsFile, comboBuilder.toString());
        }
    }

    private boolean fillOutFirstForm(WebDriver driver) {
        DriverUtils.waitUntilPageLoaded(driver);
        DriverUtils.waitUntilElementExists(driver, By.name("email"));
        DriverUtils.waitUntilElementExists(driver, By.name("username"));
        DriverUtils.waitUntilElementExists(driver, By.name("password"));
        DriverUtils.waitUntilElementExists(driver, By.xpath("//button[@type='submit']"));

        WebElement emailField, usernameField, passwordField, continueButton;

        try {
            emailField = driver.findElement(By.name("email"));
            usernameField = driver.findElement(By.name("username"));
            passwordField = driver.findElement(By.name("password"));
            continueButton = driver.findElement(By.xpath("//button[@type='submit']"));
        } catch (NoSuchElementException e) {
            Log.error("Couldn't find the required elements in the form. This thread will restart");
            return false;
        }

        Log.info("Filling out all fields");
        emailField.sendKeys(getEmail());
        usernameField.sendKeys(getUsername());
        passwordField.sendKeys(getPassword());

        Log.info("Submitting the form");
        continueButton.click();
        return true;
    }

    private boolean solveCaptcha(WebDriver driver) {
        DriverUtils.waitUntilPageLoaded(driver);
        DriverUtils.waitUntilCaptchaExists(driver);

        Log.info("Attempting to solve the captcha");
        final String siteKey = (String) executeScript(driver, CaptchaUtils.getSiteKeyScript());
        final String captchaToken = CaptchaUtils.solveCaptcha(siteKey, getPageUrl());
        final String callbackScript = CaptchaUtils.getCallbackScript(captchaToken);

        if (captchaToken.isEmpty()) {
            Log.warn("2captcha returned an empty captcha token. This thread will restart");
            return false;
        }

        Log.info("Submitting the captcha");
        executeScript(driver, callbackScript);
        return true;
    }

    /**
     * @return The file where accounts from this generator type will be saved to
     */
    @Override
    protected File getAccountsFile() {
        File accountsFile = new File(AccountGenerator.WORKING_DIRECTORY + File.separator + "discord_accounts.txt");

        int attempts = 0;
        while (!accountsFile.exists() && attempts < 6) {
            Log.info("Accounts file not found... creating it now");
            try {
                accountsFile.createNewFile();
                Log.info("Accounts file created...");
            } catch (IOException e) {
                Log.error("Could not create 'discord_accounts.txt' please make it manually.");
                Log.error("Retrying in 10 seconds...");
                attempts++;
                sleep(10000);
            }
        }

        if (!accountsFile.exists()) {
            kill();
        }

        return accountsFile;
    }

}