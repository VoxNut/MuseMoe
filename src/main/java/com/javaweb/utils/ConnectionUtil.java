package com.javaweb.utils;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class ConnectionUtil {
    private static String DB_URL = "jdbc:mysql://localhost:3306/latte_literature";
    private static String USER = "root";
    private static String PASS = "123456";

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(USER);
        config.setPassword(PASS);
        // Disable MBean registration if not done via properties
        config.setRegisterMbeans(false);
        return new HikariDataSource(config);
    }

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}