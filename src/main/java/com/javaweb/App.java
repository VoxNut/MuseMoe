package com.javaweb;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.weisj.darklaf.LafManager;
import com.javaweb.constant.AppConstant;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.LocaleUtil;
import com.javaweb.view.LoginPage;
import lombok.Getter;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

@SpringBootApplication
public class App {

    @Getter
    private static ConfigurableApplicationContext applicationContext;

    /**
     * Main entry point for the Swing application with Spring Boot integration
     */
    public static void main(String[] args) {
        // Set Spring boot to headless false for Swing
        System.setProperty("java.awt.headless", "false");

        // Start Spring Boot context with web server enabled
        applicationContext = new SpringApplicationBuilder(App.class)
                .headless(false)
                .web(WebApplicationType.SERVLET) // This starts the web server
                .run(args);

        // Configure Swing EDT
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            openLoginPage();
        });
    }

    /**
     * Configure the look and feel for the application
     */
    private static void setupLookAndFeel() {
        // Set JVM encoding to UTF-8
        System.setProperty("file.encoding", "UTF-8");

        // Configure fonts and UI
        FontUtil.setDefaultFont(AppConstant.FONT_PATH);
        FlatLightLaf.setup();
        LafManager.setDecorationsEnabled(true);

        // Configure UI manager properties
        UIManager.put("Component.arc", 500);
        UIManager.put("ProgressBar.arc", 500);
        UIManager.put("TextComponent.arc", 500);
    }

    /**
     * Open the login page with proper security context
     */
    private static void openLoginPage() {
        // Create a new security context for this instance
        SecurityContext securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        LoginPage loginPage = new LoginPage();
        UIManager.put("TitlePane.iconSize", new Dimension(20, 20));
        loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.COFFEE_SHOP_ICON_PATH, 100, 100).getImage());
        loginPage.setVisible(true);

        // Set input locale for the entire application
        LocaleUtil.setInputLocale(loginPage, new Locale("vi", "VN"));
    }

    /**
     * Get a bean from the Spring context
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
}