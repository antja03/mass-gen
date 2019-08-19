package rip.ethereal.generator.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import rip.ethereal.generator.AccountGenerator;
import rip.ethereal.generator.config.enums.CaptchaService;
import rip.ethereal.generator.config.enums.LogDensity;
import rip.ethereal.generator.config.enums.ProxyMode;
import rip.ethereal.generator.exception.ConfigNotFoundException;
import rip.ethereal.generator.exception.ProxiesNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author antja03
 */
public class Config {

    @JsonProperty("log-density")
    private LogDensity logDensity;

    @JsonProperty("show-browsers")
    private boolean showBrowsers;

    @JsonProperty("email-method")
    private String emailMode;

    @JsonProperty("master-username")
    private String masterUsername;

    @JsonProperty("master-password")
    private String masterPassword;

    @JsonProperty("proxy-mode")
    private ProxyMode proxyMode;

    @JsonProperty("rotating-proxy")
    private String rotatingProxy;

    @JsonIgnore
    private String[] proxyList;

    @JsonProperty("captcha-generator")
    private CaptchaService captchaService;

    @JsonProperty("captcha-api-key")
    private String captchaApiKey;

    @JsonProperty("threads")
    private int threads;

    @JsonProperty("domains")
    private String[] domains;

    @JsonProperty("password-length")
    private int passwordLength;

    @JsonProperty("username-dictionary")
    private String[] dictionary;

    public Config() {
        proxyList = new String[] {};
    }

    /**
     * Builds a config object based on the user made config.yml
     *
     * @return The config object
     */
    public static Config load() {
        try {
            File configFile = AccountGenerator.getConfiguratiion("config.yml");

            if (!configFile.exists())
                throw new ConfigNotFoundException();

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Config config = mapper.readValue(configFile, Config.class);

            if (config.getProxyMode().equals(ProxyMode.LIST)) {
                File proxiesFile = AccountGenerator.getConfiguratiion("proxies.txt");

                if (!proxiesFile.exists())
                    throw new ProxiesNotFoundException();

                List<String> proxies = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(proxiesFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        proxies.add(line);
                    }
                }

                config.setProxyList(proxies.toArray(config.getProxyList()));
            }

            return config;
        } catch (ConfigNotFoundException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (ProxiesNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @return The amount/type of logs that should be shown to the end user
     */
    public LogDensity getLogDensity() {
        return logDensity;
    }

    /**
     * @return Whether or not the browsers should run in headless mode
     */
    public boolean shouldShowBrowsers() {
        return showBrowsers;
    }

    /**
     * @return The method that will be used for creating verification emails
     */
    public String getEmailMode() {
        return emailMode;
    }

    /**
     * @return The username of the master email
     */
    public String getMasterUsername() {
        return masterUsername;
    }

    /**
     * @return The password of the master email
     */
    public String getMasterPassword() {
        return masterPassword;
    }

    /**
     * @return The domains that can be used for generation
     */
    public String[] getDomains() {
        return domains;
    }

    /**
     * @return The mode for how proxies are picked
     */
    public ProxyMode getProxyMode() {
        return proxyMode;
    }

    /**
     * @return A list of all usable proxies
     */
    public String[] getProxyList() {
        return proxyList;
    }

    /**
     * @param proxyList A list of all usable proxies
     */
    public void setProxyList(String[] proxyList) {
        this.proxyList = proxyList;
    }

    /**
     * @return The rotating proxy that will be used by everything
     */
    public String getRotatingProxy() {
        return rotatingProxy;
    }

    /**
     * @return The captcha service that will be used to solve any captchas
     */
    public CaptchaService getCaptchaService() {
        return captchaService;
    }

    /**
     * @return The user api key for whatever captcha service they chose
     */
    public String getCaptchaApiKey() {
        return captchaApiKey;
    }

    /**
     * @return The amount of generation threads that can be running at a time
     */
    public int getThreads() {
        return threads;
    }

    /**
     * @return The length of every password
     */
    public int getPasswordLength() {
        return passwordLength;
    }

    /**
     * @return The dictionary used for username generation
     */
    public String[] getDictionary() {
        return dictionary;
    }
}
