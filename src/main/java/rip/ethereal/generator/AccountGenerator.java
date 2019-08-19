package rip.ethereal.generator;

import org.apache.commons.lang3.RandomStringUtils;
import rip.ethereal.generator.config.Config;
import rip.ethereal.generator.config.enums.EmailMode;
import rip.ethereal.generator.config.enums.ProxyMode;
import rip.ethereal.generator.generator.impl.SteamGenerator;
import rip.ethereal.generator.util.FileUtils;
import rip.ethereal.generator.misc.VerificationEmailService;
import rip.ethereal.generator.misc.AccountWriter;
import rip.ethereal.generator.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class AccountGenerator {

    /**
     * All directories
     *
     * Working: The directory which contains the running jar
     * Config: The directory which contains all configurable files
     * Resource: The directory which contains any resources needed by the generator
     */
    //public static final File WORKING_DIRECTORY = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParentFile();
    public static final File WORKING_DIRECTORY = new File(AccountGenerator.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
    public static final File CONFIG_DIRECTORY = new File(WORKING_DIRECTORY + File.separator + "configuration");
    public static final File DEPENDENCY_DIRECTORY = new File(WORKING_DIRECTORY + File.separator + "dependencies");

    /**
     * Contains all configurable variables (object of config.yml)
     */
    public static volatile Config config;

    /**
     * Writes all generated accounts to a file
     */
    public static volatile AccountWriter accountWriter;

    /**
     * Clicks on any verification emails that are sent to the configured master email
     */
    public static volatile VerificationEmailService verificationEmailService;

    /**
     * The current index for the LIST proxy mode
     */
    private static volatile int proxyIndex = 0;

    /**
     * Starts the generator
     *
     * @param args Program arguments
     */
    public static void main(String[] args) {
        if (!verifyResources()) {
            Log.getLogger().error("The required resources and configurations could not be found...");
            Log.getLogger().error("The defaults have been created in the working directory.");
            System.exit(0);
            return;
        }

        System.setProperty("webdriver.chrome.driver", getResource("chromedriver.exe").getAbsolutePath());
        System.setProperty("webdriver.chrome.silentOutput", "true");

        config = Config.load();

        accountWriter = new AccountWriter();
        accountWriter.start();

        verificationEmailService = new VerificationEmailService();
        verificationEmailService.start();

        for (int i = 0; i < config.getThreads(); i++) {
            if (config.getEmailMode().equals(EmailMode.DOMAIN.name())) {
                new SteamGenerator().start();
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                new ProcessBuilder("cmd", "/c", "TASKKILL /IM chrome.exe /F").inheritIO().start().waitFor();
                new ProcessBuilder("cmd", "/c", "TASKKILL /IM chromedriver.exe /F").inheritIO().start().waitFor();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * Verifies that the required resources and configurations exist, and extracts the defaults from the jar
     * if they don't
     *
     * @return
     */
    private static boolean verifyResources() {
        boolean returnValue = true;

        if (!CONFIG_DIRECTORY.exists()) {
            try {
                FileUtils.extractFromClasspath("/configuration", CONFIG_DIRECTORY.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            returnValue = false;
        }

        if (!DEPENDENCY_DIRECTORY.exists()) {
            try {
                FileUtils.extractFromClasspath("/dependencies", DEPENDENCY_DIRECTORY.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            returnValue = false;
        }

        return returnValue;
    }

    /**
     * @param path The path to the file, starting from @CONFIG_DIRECTORY
     * @return The absolute path the the file
     */
    public static File getConfiguratiion(String path) {
        return new File(CONFIG_DIRECTORY + File.separator + path);
    }

    /**
     * @param path The path to the file, starting from @DEPENDENCY_DIRECTORY
     * @return The absolute path the the file
     */
    public static File getResource(String path) {
        return new File(DEPENDENCY_DIRECTORY + File.separator + path);
    }

    /**
     * Generates a random username using the configured dictionary and random number generation
     *
     * @return The generated username
     */
    public static String generateUsername() {
        int randomInteger = ThreadLocalRandom.current().nextInt(10000, 100000);
        String randomName1 = config.getDictionary()[ThreadLocalRandom.current().nextInt(0, config.getDictionary().length)];
        String randomName2 = config.getDictionary()[ThreadLocalRandom.current().nextInt(0, config.getDictionary().length)];
        return randomName1 + randomInteger + randomName2;
    }

    /**
     * Generates a random password using apache's random alphanumeric function
     *
     * @return The generated username
     */
    public static String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(config.getPasswordLength());
    }

    /**
     * Picks a random domain from the config
     *
     * @return The domain that was picked
     */
    public static String getRandomDomain() {
        return config.getDomains()[ThreadLocalRandom.current().nextInt(0, config.getDomains().length)];
    }

    public static String getProxy() {
        if (config.getProxyMode().equals(ProxyMode.LIST)) {
            if (proxyIndex > config.getProxyList().length - 1)
                proxyIndex = 0;

            String proxy = config.getProxyList()[proxyIndex];
            proxyIndex++;
            return proxy;
        } else if (config.getProxyMode().equals(ProxyMode.ROTATING)) {
            return config.getRotatingProxy();
        }

        return "";
    }
}
