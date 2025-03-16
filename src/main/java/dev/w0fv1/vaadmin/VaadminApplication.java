package dev.w0fv1.vaadmin;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class VaadminApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(VaadminApplication.class);
        app.setAdditionalProfiles("vaadmin"); // 指定激活的Profile
        app.run(args);
    }

    @Configuration
    @Push
    public static class AppShellConfig implements AppShellConfigurator {
    }

}
