package rip.ethereal.generator.misc;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import rip.ethereal.generator.AccountGenerator;
import rip.ethereal.generator.config.enums.LogDensity;
import rip.ethereal.generator.exception.SMTPNotFoundException;
import rip.ethereal.generator.util.KillableThread;
import rip.ethereal.generator.util.Log;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author antja03
 * @since 8/6/2019
 */
public class VerificationEmailService extends KillableThread {

    /**
     * The file that stores the SMTP properties
     */
    private File propertiesFile;

    /**
     * The main inbox
     */
    private Folder inbox;

    /**
     * The trash inbox (needs to be expunged to perm delete messages)
     */
    private Folder trash;

    /**
     * Constructor...
     * Connects to gmail and opens the main inbox & trash inbox
     */
    public VerificationEmailService() {
        propertiesFile = new File(AccountGenerator.CONFIG_DIRECTORY + File.separator + "smtp.properties");
        if (!propertiesFile.exists())
            try {
                throw new SMTPNotFoundException();
            } catch (SMTPNotFoundException e) {
                e.printStackTrace();
            }

        connect();
    }

    /**
     * Loops through all emails in the main inbox and verifies steam accounts
     */
    @Override
    public void run() {
        startThread();

        while (isRunning()) {
            try {
                for (Message message : inbox.getMessages()) {
                    if (message.getSubject().contains("New Steam Account")) {
                        Log.info("Verification email received");
                        if (message.isMimeType("multipart/*")) {
                            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                            verify(mimeMultipart);
                            message.setFlag(Flags.Flag.DELETED, true);
                        }
                    }
                }

                inbox.expunge();

                for (Message message : trash.getMessages()) {
                    message.setFlag(Flags.Flag.DELETED, true);
                }
                trash.expunge();
            } catch (MessagingException | IOException e) {
                if (e.getMessage().contains("Lost folder connection")) {
                    connect();
                } else {
                    e.printStackTrace();
                }
            } finally {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Filters the email content to find the verification link, then starts a chrome driver with the configured proxy
     * which then navigates to that url
     *
     * @param mimeMultipart
     * @throws MessagingException
     * @throws IOException
     */
    private void verify(MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);

            if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                Document document = Jsoup.parse(html);
                Elements linkElements = document.select("a[href]");

                for (Element element : linkElements) {
                    String elementAsString = element.toString();
                    if (elementAsString.contains("account/newaccountverification")) {
                        Log.info("Found the verification element");

                        elementAsString = StringUtils.substringsBetween(elementAsString, "\"", "\"")[0];
                        elementAsString = elementAsString.replace("amp;", "");

                        LoggingPreferences loggingPreferences = new LoggingPreferences();
                        loggingPreferences.enable(LogType.BROWSER, Level.OFF);
                        loggingPreferences.enable(LogType.SERVER, Level.OFF);
                        loggingPreferences.enable(LogType.DRIVER, Level.OFF);
                        loggingPreferences.enable(LogType.PROFILER, Level.OFF);
                        loggingPreferences.enable(LogType.CLIENT, Level.OFF);

                        ChromeOptions chromeOptions = new ChromeOptions();
                        chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
                        chromeOptions.addArguments("silent");
                        if (!AccountGenerator.config.shouldShowBrowsers())
                            chromeOptions.addArguments("headless");

                        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);

                        WebDriver webDriver = new ChromeDriver(chromeOptions);

                        if (AccountGenerator.config.getLogDensity().equals(LogDensity.ALL))
                            Log.info("Navigating to the verification url");

                        webDriver.get(elementAsString);

                        try {
                            WebElement webElement = webDriver.findElement(By.xpath("//td[@style='background: #7E7E7E;height:32px']//a"));
                            webDriver.navigate().to(webElement.getAttribute("href"));
                        } catch (NoSuchElementException e) {
                            // Handle silently
                        }

                        try {
                            WebElement webElement = webDriver.findElement(By.xpath("//td[@style='background: #799905;height: 32px;text-align: center' and @align='center']//a"));
                            webDriver.navigate().to(webElement.getAttribute("href"));
                        } catch (NoSuchElementException e) {
                            // Handle silently
                        }

                        try {
                            sleep(7500);
                        } catch (InterruptedException e) {
                            //Sleep should never throw an exception, so we're gonna go ahead and print that.
                            e.printStackTrace();
                        }

                        webDriver.quit();
                    }
                }

            }
        }
    }

    private void connect() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));
            Session session = Session.getDefaultInstance(properties, null);

            Store store = session.getStore("imaps");
            store.connect("smtp.gmail.com",
                    String.format("%s@gmail.com", AccountGenerator.config.getMasterUsername()),
                    AccountGenerator.config.getMasterPassword());

            inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            trash = store.getFolder("[Gmail]/Trash");
            trash.open(Folder.READ_WRITE);
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
