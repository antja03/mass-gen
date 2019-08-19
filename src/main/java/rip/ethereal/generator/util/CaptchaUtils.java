package rip.ethereal.generator.util;

import com.twocaptcha.api.TwoCaptchaService;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import rip.ethereal.generator.AccountGenerator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author antja03
 */
public class CaptchaUtils {

    public static String getCallbackScript(String token) {
        final StringBuilder builder = new StringBuilder();
        builder.append("___grecaptcha_cfg.clients[0].NY.N.callback(\"");
        builder.append(token);
        builder.append("\")");
        return builder.toString();
    }

    public static String getSiteKeyScript() {
        return "return ___grecaptcha_cfg.clients[0].NY.N.sitekey";
    }

    /**
     * Sends the captcha to 2captcha and retrieves the response token
     *
     * @param googleKey The captcha key/site key
     * @param pageUrl The url of the page that contains the captcha
     * @return The captcha token (response token)
     */
    public static String solveCaptcha(String googleKey, String pageUrl) {
        try {
            TwoCaptchaService service = new TwoCaptchaService(AccountGenerator.config.getCaptchaApiKey(), googleKey, pageUrl);
            return service.solveCaptcha();
        } catch (InterruptedException | IOException e) {
            // Handle silently
        }

        return "";
    }

    /**
     * Retrieves the google key from the provided web element
     *
     * @param captchaElement The captcha element
     * @return The google key
     */
    public static String retrieveKey(WebElement captchaElement) {
        String url = captchaElement.getAttribute("src");
        Optional<String> optionalKey = Arrays.stream(url.split("&"))
                .filter(s -> s.startsWith("k="))
                .map(s -> s.replace("k=", ""))
                .findFirst();

        if (optionalKey.isPresent()) {
            return optionalKey.get();
        } else {
            return "";
        }
    }

}
