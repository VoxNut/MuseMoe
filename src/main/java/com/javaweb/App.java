package com.javaweb;

import lombok.Getter;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class App {

    @Getter
    private static ConfigurableApplicationContext applicationContext;


    public static void main(String[] args) {


        System.setProperty("java.awt.headless", "false");

        applicationContext = new SpringApplicationBuilder(App.class)
                .headless(false)
                .web(WebApplicationType.SERVLET)
                .run(args);

        MusicAppUI.main(args);
    }

    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
}