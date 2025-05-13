package com.javaweb;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.weisj.darklaf.LafManager;
import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.*;
import com.javaweb.view.HomePage;
import com.javaweb.view.LoginPage;
import com.javaweb.view.user.UserSessionManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.swing.*;
import java.awt.*;

public class MusicAppUI {

    public static void main(String[] args) {
        MusicAppUI ui = new MusicAppUI();
        ui.launch();
    }


    public void launch() {
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            openLoginPage();
        });
    }


    private void setupLookAndFeel() {
        System.setProperty("file.encoding", "UTF-8");

        FontUtil.setDefaultFont(AppConstant.FONT_PATH);
        FlatLightLaf.setup();
        LafManager.setDecorationsEnabled(true);

        UIManager.put("TitlePane.iconSize", new Dimension(24, 24));
        UIManager.put("Button.focusWidth", 0);
        UIManager.put("Button.focusColor", new Color(0, 0, 0, 0));
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Button.shadowWidth", 0);
        GuiUtil.disableJaudiotaggerLogging();
    }

    private void openLoginPage() {

        String savedToken = TokenStorage.loadToken();
        if (savedToken != null) {
            try {
                // Validate token
                UserDTO user = JwtTokenUtil.extractUserFromToken(savedToken);

                boolean valid = validateTokenWithServer(savedToken);

                if (user != null && valid) {
                    UserSessionManager.getInstance().initializeSession(user, savedToken);

                    SwingUtilities.invokeLater(() -> {
                        HomePage homePage = new HomePage();
                        homePage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 512, 512).getImage());
                        homePage.setVisible(true);
                    });
                    return;
                } else {
                    TokenStorage.clearToken();
                }
            } catch (Exception e) {
                TokenStorage.clearToken();
            }
        }

        LoginPage loginPage = new LoginPage();
        loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 512, 512).getImage());

        loginPage.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        loginPage.setVisible(true);
    }

    /**
     * Validate token with the server
     */
    private boolean validateTokenWithServer(String token) {
        try {
            CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();

            HttpGet request = new HttpGet("http://localhost:8081/api/user/me");
            request.setHeader("Authorization", "Bearer " + token);

            CloseableHttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            response.close();

            return statusCode == 200;
        } catch (Exception e) {
            return false;
        }
    }
}