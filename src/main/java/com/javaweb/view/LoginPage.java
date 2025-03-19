package com.javaweb.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.RoleDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.*;
import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LoginPage extends JFrame {
    private static final Dimension FRAME_SIZE = new Dimension(1100, 934);
    private static final String IMAGE_PATH = "src/main/java/com/javaweb/view/imgs/back_ground/Latte_Literature.png";
    private CardLayout cardLayout;
    private JPanel mainPanel;
    @Getter
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private JLabel statusLabel;
    private JLabel forgotPasswordLabel;
    private AuthenticationManager authenticationManager;

    public LoginPage() {
        initializeFrame();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(createMainPanel(), "login");
        mainPanel.add(createSignUpPanel(), "signup");
        add(mainPanel);
        addEventListeners();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            UIManager.put("TitlePane.iconSize", new Dimension(20, 20));
            loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.COFFEE_SHOP_ICON_PATH, 100, 100).getImage());
            loginPage.setVisible(true);
        });
    }

    private void initializeFrame() {
        setSize(FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(FRAME_SIZE);
        setMaximumSize(FRAME_SIZE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(AppConstant.BACKGROUND_COLOR);
        mainPanel.add(createImagePanel());
        mainPanel.add(createRightPanel());
        return mainPanel;
    }

    private JPanel createImagePanel() {
        ImageIcon imageIcon = new ImageIcon(IMAGE_PATH);
        JLabel imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                double scale = Math.min(
                        (double) getWidth() / imageIcon.getIconWidth(),
                        (double) getHeight() / imageIcon.getIconHeight());
                int width = (int) (imageIcon.getIconWidth() * scale);
                int height = (int) (imageIcon.getIconHeight() * scale);
                int x = (getWidth() - width) / 2;
                int y = (getHeight() - height) / 2;
                g.drawImage(imageIcon.getImage(), x, y, width, height, this);
            }
        };

        // Create overlay panel
        JPanel overlayPanel = new JPanel();
        overlayPanel.setOpaque(false);
        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
        overlayPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 'Sign up' button
        JButton signUpButton = createButton("<html><div style=\"text-align: center;\">Bạn là khách hàng?<br>ĐĂNG KÝ NGAY!</div></html>");
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.setMaximumSize(new Dimension(250, signUpButton.getPreferredSize().height));
        signUpButton.addActionListener(e -> cardLayout.show(mainPanel, "signup"));

        // Add components to overlay panel
        overlayPanel.add(Box.createVerticalGlue());
        overlayPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        overlayPanel.add(signUpButton);
        overlayPanel.add(Box.createVerticalGlue());

        // Layered pane to stack image and overlay
        JLayeredPane layeredPane = new JLayeredPane();
        imageLabel.setBounds(0, 0, getWidth() / 2, getHeight());
        overlayPanel.setBounds(0, 0, getWidth() / 2, getHeight());

        layeredPane.add(imageLabel, Integer.valueOf(0));
        layeredPane.add(overlayPanel, Integer.valueOf(1));
        JPanel imagePanel = new JPanel(new BorderLayout()) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                imageLabel.setSize(getSize());
            }
        };
        imagePanel.add(layeredPane, BorderLayout.CENTER);

        return imagePanel;
    }

    private JPanel createSignUpPanel() {
        JPanel signUpPanel = new JPanel(new GridLayout(1, 2));
        signUpPanel.setBackground(AppConstant.BACKGROUND_COLOR);
        signUpPanel.add(createSignUpFormPanel()); // Left side
        signUpPanel.add(createImagePanelWithSignInButton()); // Right side
        return signUpPanel;
    }

    private JPanel createSignUpFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(AppConstant.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel signUpTitle = new JLabel("ĐĂNG KÝ", SwingConstants.CENTER);
//        signUpTitle.putClientProperty( "FlatLaf.styleClass", "h1" );
        signUpTitle.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 55));

        signUpTitle.setForeground(AppConstant.TEXT_COLOR);
        signUpTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 60, 0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(signUpTitle, gbc);

        // Username field
        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel usernameLabel = GuiUtil.createLabel("Username:", Font.PLAIN, 20);
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        JTextField usernameField = GuiUtil.createLineInputField(AppConstant.TEXT_FIELD_SIZE);
        formPanel.add(usernameField, gbc);

        // Email field
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel emailLabel = GuiUtil.createLabel("Email:", Font.PLAIN, 20);
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        JTextField emailField = GuiUtil.createLineInputField(AppConstant.TEXT_FIELD_SIZE);
        formPanel.add(emailField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = GuiUtil.createLabel("Password:", Font.PLAIN, 20);
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = GuiUtil.createLineInputPasswordField(AppConstant.TEXT_FIELD_SIZE);
        formPanel.add(passwordField, gbc);

        // Add KeyListener to trigger sign-up on Enter key press
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSignUp(usernameField.getText(), emailField.getText(),
                            new String(passwordField.getPassword()));
                }
            }
        });

        // 'Sign up' button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton signUpButton = createButton("Đăng ký");
        signUpButton.addActionListener(e -> handleSignUp(usernameField.getText(), emailField.getText(),
                new String(passwordField.getPassword())));
        formPanel.add(signUpButton, gbc);

        return formPanel;
    }

    private JPanel createImagePanelWithSignInButton() {
        ImageIcon imageIcon = new ImageIcon(IMAGE_PATH);
        JLabel imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Scale image to fill the panel while maintaining aspect ratio
                double scale = Math.max(
                        (double) getWidth() / imageIcon.getIconWidth(),
                        (double) getHeight() / imageIcon.getIconHeight());
                int width = (int) (imageIcon.getIconWidth() * scale);
                int height = (int) (imageIcon.getIconHeight() * scale);
                g.drawImage(imageIcon.getImage(), 0, 0, width, height, this);
            }
        };

        // Create overlay panel for sign in button
        JPanel overlayPanel = new JPanel();
        overlayPanel.setOpaque(false);
        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
        overlayPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create and configure sign in button
        JButton signInButton = createButton("Đăng nhập");
        signInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        // Add components to overlay
        overlayPanel.add(Box.createVerticalGlue());
        overlayPanel.add(signInButton);
        overlayPanel.add(Box.createVerticalGlue());

        // Use JLayeredPane to stack image and button
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Update bounds when resized
                imageLabel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                overlayPanel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
            }
        });

        layeredPane.add(imageLabel, Integer.valueOf(0));
        layeredPane.add(overlayPanel, Integer.valueOf(1));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(layeredPane, BorderLayout.CENTER);

        return imagePanel;
    }

    private void handleSignUp(String username, String email, String password) {
        if (!validateInputFields(username, email, password)) {
            return;
        }
        if (CommonApiUtil.fetchUserbyUsername(username) != null) {
            GuiUtil.showWarningMessageDialog(LoginPage.this, "Username đã tồn tại vui lòng chọn một username khác!");
            return;
        }

        try {
            CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
            HttpPost httpPost = new HttpPost("http://localhost:8081/api/user/register");
            httpPost.setHeader("Content-Type", "application/json");

            // Create JSON object with user data
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();
            json.put("username", username);
            json.put("email", email);
            json.put("password", password);

            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.CREATED.value()) {
                GuiUtil.showSuccessMessageDialog(this, "Tạo tài khoản thành công. Bạn giờ có thể đăng nhập");
                cardLayout.show(mainPanel, "login");
            } else {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                GuiUtil.showErrorMessageDialog(this, "Lỗi khi tạo tài khoản " + responseBody);
            }

            response.close();
        } catch (Exception e) {
            e.printStackTrace();
            GuiUtil.showErrorMessageDialog(this, "Một lỗi đã xảy ra khi đăng ký.");
        }
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(AppConstant.BACKGROUND_COLOR);
        rightPanel.add(createFormPanel(), BorderLayout.CENTER);
        return rightPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(AppConstant.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        addLoginTitle(formPanel, gbc);
        addUsernameField(formPanel, gbc);
        addPasswordField(formPanel, gbc);
        addButtonsPanel(formPanel, gbc);
        addStatusLabel(formPanel, gbc);
        addForgotPasswordLink(formPanel, gbc);

        return formPanel;
    }

    private void addLoginTitle(JPanel formPanel, GridBagConstraints gbc) {
        JLabel loginTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        loginTitle.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 55));
        loginTitle.setForeground(AppConstant.TEXT_COLOR);
        loginTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 60, 0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(loginTitle, gbc);
    }

    private void addUsernameField(JPanel formPanel, GridBagConstraints gbc) {
        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel usernameLabel = GuiUtil.createLabel("Username:", Font.PLAIN, 20);
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        usernameField = GuiUtil.createLineInputField(AppConstant.TEXT_FIELD_SIZE);

        formPanel.add(usernameField, gbc);
    }

    private void addPasswordField(JPanel formPanel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = GuiUtil.createLabel("Password:", Font.PLAIN, 20);
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        passwordField = GuiUtil.createLineInputPasswordField(AppConstant.TEXT_FIELD_SIZE);
        formPanel.add(passwordField, gbc);
    }

    private void addButtonsPanel(JPanel formPanel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setBackground(AppConstant.BACKGROUND_COLOR);
        loginButton = createButton("Đăng nhập");
        exitButton = createButton("Thoát");
        buttonsPanel.add(loginButton);
        buttonsPanel.add(exitButton);
        formPanel.add(buttonsPanel, gbc);
    }

    private void addStatusLabel(JPanel formPanel, GridBagConstraints gbc) {
        gbc.gridy++;
        statusLabel = new JLabel("");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(AppConstant.TEXT_COLOR);
        formPanel.add(statusLabel, gbc);
    }

    private void addForgotPasswordLink(JPanel formPanel, GridBagConstraints gbc) {
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        forgotPasswordLabel = new JLabel("Quên mật khẩu?");
        forgotPasswordLabel.setFont(FontUtil.getJetBrainsMonoFont(Font.ITALIC, 13));
        forgotPasswordLabel.setForeground(AppConstant.TEXT_COLOR);
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension size = forgotPasswordLabel.getPreferredSize();
        forgotPasswordLabel.setPreferredSize(size);
        formPanel.add(forgotPasswordLabel, gbc);
    }

    private void addEventListeners() {

        loginButton.addActionListener(e -> {
            try {
                authenticateUser(usernameField.getText(), new String(passwordField.getPassword()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        authenticateUser(usernameField.getText(), new String(passwordField.getPassword()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        exitButton.addActionListener(e -> System.exit(0));

        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
        });
    }

    private void handleForgotPassword() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            GuiUtil.showWarningMessageDialog(this, "Vui lòng nhập Username!.");
            return;
        }

        try {
            CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
            // Fetch user details by username
            HttpGet httpGet = new HttpGet(
                    "http://localhost:8081/api/user/username/" + URLEncoder.encode(username, "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ObjectMapper objectMapper = new ObjectMapper();
                UserDTO user = objectMapper.readValue(responseBody, UserDTO.class);

                String userEmail = user.getEmail();

                // Generate a temporary password
                String tempPassword = generateTemporaryPassword();

                // Update the user's password in the database
                updatePasswordInDatabase(user.getId(), tempPassword);

                // Send email with the temporary password
                SendEmailUtil.sendEmailAsync(userEmail, tempPassword);

                GuiUtil.showInfomationMessageDialog(this, "Một email kèm hướng dẫn đã được gửi cho bạn.");
            } else if (statusCode == 500) {
                GuiUtil.showErrorMessageDialog(this, "Không tìm thấy người dùng.");
            } else {
                GuiUtil.showErrorMessageDialog(this, "Một lỗi không mong muốn đã xảy ra, vui lòng thử lại sau.");
            }
            response.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            GuiUtil.showErrorMessageDialog(this, "Một lỗi đã xảy ra trong khi thực hiện yêu cầu của bạn.");
        }
    }

    // Method to generate a temporary password
    private String generateTemporaryPassword() {
        // Implement a secure way to generate a temporary password
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Method to update the password in the database
    private void updatePasswordInDatabase(Long userId, String tempPassword) throws IOException {
        CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
        HttpPut httpPut = new HttpPut("http://localhost:8081/api/user/" + userId + "/reset-password");
        httpPut.setHeader("Content-Type", "application/json");

        // Create a JSON object with the new password
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("newPassword", tempPassword);

        StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
        httpPut.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(httpPut);
        response.close();
    }

    private boolean validateInputFields(String username, String email, String password) {
        if (!ValidateUtil.isValidUsername(username)) {
            GuiUtil.showWarningMessageDialog(this, "Username không hợp lệ.");
            return false;
        }
        if (email != null) {
            if (!ValidateUtil.isValidEmail(email)) {
                GuiUtil.showWarningMessageDialog(this, "Hãy nhập một email phù hợp.");
                return false;
            }
        }
        if (!ValidateUtil.isValidPassword(password)) {
            GuiUtil.showWarningMessageDialog(this, "Password không hợp lệ.");
            return false;
        }
        return true;
    }


    private void authenticateUser(String username, String password) {
        if (!validateInputFields(username, null, password)) {
            return;
        }

        try {
            CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
            HttpPost httpPost = new HttpPost("http://localhost:8081/login");
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            CloseableHttpResponse response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 302 || statusCode == 200) {
                // Authentication successful
                statusLabel.setText("Login successful!");
                // Fetch current user details
                HttpGet userGet = new HttpGet("http://localhost:8081/api/user/me");
                CloseableHttpResponse userResponse = httpClient.execute(userGet);
                int userStatusCode = userResponse.getStatusLine().getStatusCode();
                if (userStatusCode == 200) {
                    String userResponseBody = EntityUtils.toString(userResponse.getEntity(), StandardCharsets.UTF_8);
                    ObjectMapper objectMapper = new ObjectMapper();
                    UserDTO user = objectMapper.readValue(userResponseBody, UserDTO.class);
                    if (user.getStatus() == 0) {
                        statusLabel.setText("Người dùng không tồn tại hoặc đã bị xóa.");
                        response.close();
                    }
                    String avatarLink = user.getAvatar();
                    String userFullName = user.getFullName();
                    Set<String> roles = new HashSet<>();
                    for (RoleDTO role : user.getRoles()) {
                        roles.add("ROLE_" + role.getCode());
                    }
                    userResponse.close();
                    response.close();
                    this.dispose();
                    HomePage homePage = new HomePage(avatarLink, userFullName, roles, user);
                    UIManager.put("TitlePane.iconSize", new Dimension(20, 20));
                    homePage.setIconImage(GuiUtil.createImageIcon(AppConstant.COFFEE_SHOP_ICON_PATH, 100, 100).getImage());
                    homePage.setVisible(true);
                } else {
                    // Authentication failed
                    statusLabel.setText("username hoặc password không đúng.");
                    response.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Một lỗi đã xảy ra khi đăng nhập.");
        }
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(AppConstant.BUTTON_BACKGROUND_COLOR);
        button.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 16));
        button.setForeground(AppConstant.BUTTON_TEXT_COLOR);
        return button;
    }

}