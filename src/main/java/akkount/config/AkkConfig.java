/*
 * Copyright (c) 2015 akkount
 */

package akkount.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "akk")
public class AkkConfig {

    private String slackToken;
    private String slackVerificationToken;
    private String slackUserLogin;
    private String defaultAccount;
    private String defaultCategory;

    public AkkConfig(
            String slackToken,
            String slackVerificationToken,
            @DefaultValue("slack") String slackUserLogin,
            @DefaultValue("cash") String defaultAccount,
            @DefaultValue("food") String defaultCategory
    ) {
        this.slackToken = slackToken;
        this.slackVerificationToken = slackVerificationToken;
        this.slackUserLogin = slackUserLogin;
        this.defaultAccount = defaultAccount;
        this.defaultCategory = defaultCategory;
    }

    public String getSlackToken() {
        return slackToken;
    }

    public String getSlackVerificationToken() {
        return slackVerificationToken;
    }

    public String getSlackUserLogin() {
        return slackUserLogin;
    }

    public String getDefaultAccount() {
        return defaultAccount;
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }
}
