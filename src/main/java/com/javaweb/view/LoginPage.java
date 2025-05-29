package com.javaweb.view;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.*;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

@Slf4j
public class LoginPage extends JFrame {
    private static final Dimension FRAME_SIZE = new Dimension(1100, 934);
    private static final String IMAGE_PATH = "src/main/java/com/javaweb/view/imgs/back_ground/dark-blossom.jpg";
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    @Getter
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private JLabel forgotPasswordLabel;

    private final Color backgroundColor = ThemeManager.getInstance().getBackgroundColor();
    private final Color textColor = ThemeManager.getInstance().getTextColor();

    public LoginPage() {
        initializeFrame();
        cardLayout = new CardLayout();
        mainPanel = GuiUtil.createPanel(cardLayout);
        mainPanel.add(createMainPanel(), "login");
        mainPanel.add(createSignUpPanel(), "signup");

        add(mainPanel);
        addEventListeners();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            UIManager.put("TitlePane.iconSize", new Dimension(20, 20));
            loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 100, 100).getImage());
            loginPage.setVisible(true);
        });
    }

    private void initializeFrame() {
        setSize(FRAME_SIZE);
        setMinimumSize(FRAME_SIZE);
        setMaximumSize(FRAME_SIZE);
        setResizable(false);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        GuiUtil.styleTitleBar(this, GuiUtil.lightenColor(backgroundColor, 0.12), textColor);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int option = GuiUtil.showConfirmMessageDialog(
                        LoginPage.this,
                        "Do you really want to exit MuseMoe? We'll miss you :(",
                        "Exit"
                );
                if (option == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = GuiUtil.createPanel(new GridLayout(1, 2));
        mainPanel.setOpaque(true);
        mainPanel.setBackground(backgroundColor);
        mainPanel.add(createImagePanel());
        mainPanel.add(createRightPanel());
        return mainPanel;
    }


    private JPanel createSignUpPanel() {
        JPanel signUpPanel = GuiUtil.createPanel(new GridLayout(1, 2));
        signUpPanel.setOpaque(true);
        signUpPanel.setBackground(backgroundColor);
        signUpPanel.add(createSignUpFormPanel()); // Left side
        signUpPanel.add(createImagePanelWithSignInButton()); // Right side
        return signUpPanel;
    }

    private JPanel createSignUpFormPanel() {
        JPanel formPanel = GuiUtil.createPanel(new GridBagLayout());
        formPanel.setOpaque(true);
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel signUpTitle = new JLabel("REGISTER", SwingConstants.CENTER);
        signUpTitle.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 55));

        signUpTitle.setForeground(textColor);
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

        // Full name field
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel fullNameLabel = GuiUtil.createLabel("Full Name:", Font.PLAIN, 20);
        formPanel.add(fullNameLabel, gbc);
        gbc.gridx = 1;
        JTextField fullNameField = GuiUtil.createLineInputField(AppConstant.TEXT_FIELD_SIZE);
        formPanel.add(fullNameField, gbc);

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


        gbc.gridx = 0;
        gbc.gridy++;
        JLabel confirmPasswordLabel = GuiUtil.createLabel("Confirm Password:", Font.PLAIN, 20);
        formPanel.add(confirmPasswordLabel, gbc);
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = GuiUtil.createLineInputPasswordField(AppConstant.TEXT_FIELD_SIZE);
        formPanel.add(confirmPasswordField, gbc);


        // Add KeyListener to trigger sign-up on Enter key press
        confirmPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSignUp(usernameField.getText(), fullNameField.getText(), emailField.getText(),
                            new String(passwordField.getPassword()), new String(confirmPasswordField.getPassword()));
                }
            }
        });

        // 'Sign up' button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton signUpButton = GuiUtil.createPlainButton("Register");
        signUpButton.addActionListener(e -> handleSignUp(usernameField.getText(), fullNameField.getText(), emailField.getText(),
                new String(passwordField.getPassword()), new String(confirmPasswordField.getPassword())));
        formPanel.add(signUpButton, gbc);

        return formPanel;
    }

    private JPanel createImagePanel() {
        // Use a properly loaded high-quality image
        ImageIcon originalIcon = new ImageIcon(IMAGE_PATH);
        Image originalImage = originalIcon.getImage();

        // Create a panel with custom painting for smooth scaling
        JPanel imagePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Use Graphics2D for better quality rendering
                Graphics2D g2d = (Graphics2D) g.create();
                GuiUtil.configureGraphicsForHighQuality(g2d);

                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Calculate scale to fill the panel while maintaining aspect ratio
                double imageWidth = originalImage.getWidth(this);
                double imageHeight = originalImage.getHeight(this);
                double panelWidth = getWidth();
                double panelHeight = getHeight();

                double scale = Math.max(
                        panelWidth / imageWidth,
                        panelHeight / imageHeight
                );

                // Calculate dimensions and position to center the image
                int scaledWidth = (int) (imageWidth * scale);
                int scaledHeight = (int) (imageHeight * scale);
                int x = (int) ((panelWidth - scaledWidth) / 2);
                int y = (int) ((panelHeight - scaledHeight) / 2);

                // Draw the scaled image
                g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, this);

                // Add a subtle darkening overlay for better text readability
                g2d.setColor(new Color(0, 0, 0, 60)); // Black with 60/255 alpha
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.dispose();
            }
        };

        // Create overlay panel for the register button
        JPanel overlayPanel = GuiUtil.createPanel();
        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));

        // 'Sign up' button
        JButton signUpButton = GuiUtil.createPlainButton("<html><div style=\"text-align: center;\">Haven't got an account yet?<br>REGISTER NOW!</div></html>");
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.setMaximumSize(new Dimension(250, signUpButton.getPreferredSize().height));
        signUpButton.addActionListener(e -> cardLayout.show(mainPanel, "signup"));

        // Add components to overlay panel
        overlayPanel.add(Box.createVerticalGlue());
        overlayPanel.add(signUpButton);
        overlayPanel.add(Box.createVerticalGlue());

        // Add the overlay using a layered pane
        JLayeredPane layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                // Ensure components are properly sized when the panel is resized
                for (Component c : getComponents()) {
                    c.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        };

        layeredPane.add(imagePanel, Integer.valueOf(0));
        layeredPane.add(overlayPanel, Integer.valueOf(1));

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(layeredPane, BorderLayout.CENTER);

        return containerPanel;
    }

    private JPanel createImagePanelWithSignInButton() {
        // Use a properly loaded high-quality image (same as login panel)
        ImageIcon originalIcon = new ImageIcon(IMAGE_PATH);
        Image originalImage = originalIcon.getImage();

        // Create a panel with custom painting (identical to the login panel for consistency)
        JPanel imagePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Use Graphics2D for better quality rendering
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Fill with black in case of letterboxing
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Calculate scale to fill the panel while maintaining aspect ratio
                double imageWidth = originalImage.getWidth(this);
                double imageHeight = originalImage.getHeight(this);
                double panelWidth = getWidth();
                double panelHeight = getHeight();

                double scale = Math.max(
                        panelWidth / imageWidth,
                        panelHeight / imageHeight
                );

                // Calculate dimensions and position to center the image
                int scaledWidth = (int) (imageWidth * scale);
                int scaledHeight = (int) (imageHeight * scale);
                int x = (int) ((panelWidth - scaledWidth) / 2);
                int y = (int) ((panelHeight - scaledHeight) / 2);

                // Draw the scaled image
                g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, this);

                // Add a subtle darkening overlay for better text readability
                g2d.setColor(new Color(0, 0, 0, 60)); // Black with 60/255 alpha
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.dispose();
            }
        };

        // Create overlay panel for login button
        JPanel overlayPanel = new JPanel();
        overlayPanel.setOpaque(false);
        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));

        // Create and configure sign in button
        JButton signInButton = GuiUtil.createPlainButton("Login");
        signInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        // Add components to overlay
        overlayPanel.add(Box.createVerticalGlue());
        overlayPanel.add(signInButton);
        overlayPanel.add(Box.createVerticalGlue());

        // Use JLayeredPane with the same layout logic as the login panel
        JLayeredPane layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                // Ensure components are properly sized when the panel is resized
                for (Component c : getComponents()) {
                    c.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        };

        layeredPane.add(imagePanel, Integer.valueOf(0));
        layeredPane.add(overlayPanel, Integer.valueOf(1));

        JPanel containerPanel = GuiUtil.createPanel(new BorderLayout());
        containerPanel.setOpaque(true);
        containerPanel.add(layeredPane, BorderLayout.CENTER);

        return containerPanel;
    }

    private void handleSignUp(String username, String fullName, String email, String password, String confirmPassword) {
        if (!validateInputFields(username, email, password, confirmPassword)) {
            return;
        }
        if (CommonApiUtil.fetchUserByUsername(username) != null) {
            GuiUtil.showWarningMessageDialog(LoginPage.this, "username existed, please choose another username!");
            return;
        }
        if (CommonApiUtil.fetchUserByEmail(email) != null) {
            GuiUtil.showWarningMessageDialog(LoginPage.this, "email registered, please choose another email!");
            return;
        }


        if (CommonApiUtil.createNewUser(username, fullName, password, email)) {
            GuiUtil.showSuccessMessageDialog(this, "Create account successfully. You can log in now! :)");
            cardLayout.show(mainPanel, "login");
        } else {
            GuiUtil.showErrorMessageDialog(this, "An error has occurred when creating account! ");
        }


    }

    private JPanel createRightPanel() {
        JPanel rightPanel = GuiUtil.createPanel(new BorderLayout());
        rightPanel.setOpaque(true);
        rightPanel.setBackground(backgroundColor);
        rightPanel.add(createFormPanel(), BorderLayout.CENTER);
        return rightPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        addLoginTitle(formPanel, gbc);
        addUsernameField(formPanel, gbc);
        addPasswordField(formPanel, gbc);
        addButtonsPanel(formPanel, gbc);
        addForgotPasswordLink(formPanel, gbc);

        return formPanel;
    }

    private void addLoginTitle(JPanel formPanel, GridBagConstraints gbc) {
        JLabel loginTitle = new JLabel("LOGIN", SwingConstants.CENTER);
        loginTitle.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 55));
        loginTitle.setForeground(textColor);
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
        JPanel buttonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setOpaque(true);
        buttonsPanel.setBackground(backgroundColor);

        loginButton = GuiUtil.createPlainButton("Login");
        exitButton = GuiUtil.createPlainButton("Exit");


        buttonsPanel.add(loginButton);
        buttonsPanel.add(exitButton);

        formPanel.add(buttonsPanel, gbc);
    }


    private void addForgotPasswordLink(JPanel formPanel, GridBagConstraints gbc) {
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        forgotPasswordLabel = GuiUtil.createInteractiveLabel("Forgot your password?", Font.ITALIC, 13);
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
        exitButton.addActionListener(e -> {
            int option = GuiUtil.showConfirmMessageDialog(this, "Do you really want to exit MuseMoe? We'll miss you :(", "Exit");
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
        });
    }

    private void handleForgotPassword() {
        int option = GuiUtil.showConfirmMessageDialog(mainPanel,
                "You're going to receive an email with reset instructions. Do you want to continue?",
                "Reset password");

        if (option == JOptionPane.YES_OPTION) {
            String username = usernameField.getText().trim();

            if (username.isEmpty()) {
                GuiUtil.showWarningMessageDialog(this, "Please enter Username!");
                return;
            }

            // Disable controls and show progress
            forgotPasswordLabel.setEnabled(false);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                private String errorMessage;

                @Override
                protected Boolean doInBackground() {
                    try {
                        UserDTO user = CommonApiUtil.fetchUserByUsername(username);

                        if (user == null) {
                            errorMessage = "This user does not exist!";
                            return false;
                        }

                        String userEmail = user.getEmail();

                        // Generate a temporary password
                        String tempPassword = generateTemporaryPassword();

                        // Update the user's password in the database
                        if (!updatePasswordInDatabase(user.getId(), tempPassword)) {
                            errorMessage = "Failed to update password!";
                            return false;
                        }

                        // Send email with the temporary password
                        SendEmailUtil.sendEmail(userEmail, tempPassword);
                        return true;
                    } catch (Exception e) {
                        log.error("Error in password reset", e);
                        errorMessage = "An error occurred: " + e.getMessage();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    forgotPasswordLabel.setEnabled(true);

                    try {
                        boolean success = get();
                        if (success) {
                            GuiUtil.showInfoMessageDialog(LoginPage.this,
                                    "A temporary password has been sent to your email address.");
                        } else {
                            GuiUtil.showErrorMessageDialog(LoginPage.this, errorMessage);
                        }
                    } catch (Exception e) {
                        log.error("Error completing password reset", e);
                        GuiUtil.showErrorMessageDialog(LoginPage.this,
                                "An unexpected error occurred. Please try again later.");
                    }
                }
            };

            worker.execute();
        }
    }

    // Method to generate a temporary password
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean updatePasswordInDatabase(Long userId, String tempPassword) {
        return CommonApiUtil.updateUserPassword(userId, tempPassword);
    }

    private boolean validateInputFields(String username, String email, String password, String confirmPassword) {
        if (!ValidateUtil.isValidUsername(username)) {
            GuiUtil.showWarningMessageDialog(this, "Username not valid.");
            return false;
        }
        if (email != null) {
            if (!ValidateUtil.isValidEmail(email)) {
                GuiUtil.showWarningMessageDialog(this, "Please try a proper email.");
                return false;
            }
        }
        if (confirmPassword != null) {
            if (!password.equals(confirmPassword)) {
                GuiUtil.showWarningMessageDialog(this, "Confirm password doesn't match with password.");
                return false;
            }
        }


        if (!ValidateUtil.isValidPassword(password)) {
            GuiUtil.showWarningMessageDialog(this, "Password not valid.");
            return false;
        }

        return true;
    }


    private void authenticateUser(String username, String password) {
        if (!validateInputFields(username, null, password, null)) {
            return;
        }


        SwingWorker<LoginResult, Void> worker = new SwingWorker<>() {
            @Override
            protected LoginResult doInBackground() {
                try {
                    String url = "http://localhost:8081/api/auth/login";
                    HttpPost httpPost = new HttpPost(url);

                    // Create JSON for body
                    String jsonBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
                    StringEntity entity = new StringEntity(jsonBody);
                    httpPost.setEntity(entity);
                    httpPost.setHeader("Content-type", "application/json");

                    CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
                    CloseableHttpResponse response = httpClient.execute(httpPost);

                    int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        String responseBody = reader.readLine();

                        String token = responseBody.split("\"token\":\"")[1].split("\"")[0];
                        UserDTO user = JwtTokenUtil.extractUserFromToken(token);

                        return new LoginResult(true, user, token, null);
                    } else if (statusCode == 401 || statusCode == 404) {
                        return new LoginResult(false, null, null, "Username or Password incorrect!");
                    } else {
                        return new LoginResult(false, null, null, "Error connecting to server!");
                    }
                } catch (Exception e) {
                    log.error("Authentication error", e);
                    return new LoginResult(false, null, null, "Error: " + e.getMessage());
                }
            }

            @Override
            protected void done() {
                try {

                    LoginResult result = get();

                    if (result.success) {
                        UserDTO user = result.user;
                        String token = result.token;

                        log.info("username '{}', with token '{}'", user.getUsername(), token);

                        // Store token in UserSessionManager
                        UserSessionManager.getInstance().initializeSession(user, token);
                        // Save token for auto-login on next start
                        TokenStorage.saveToken(token, user);

                        GuiUtil.showSuccessMessageDialog(LoginPage.this, "Login successful!");
                        dispose();

                        // Create the HomePage
                        SwingUtilities.invokeLater(() -> {
                            HomePage homePage = new HomePage();
                            UIManager.put("TitlePane.iconSize", new Dimension(24, 24));
                            homePage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 512, 512).getImage());
                            homePage.setVisible(true);
                        });
                    } else {
                        GuiUtil.showErrorMessageDialog(LoginPage.this, result.errorMessage);
                    }
                } catch (Exception e) {
                    log.error("Error processing login result", e);
                    GuiUtil.showErrorMessageDialog(LoginPage.this, "Unexpected error: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private static class LoginResult {
        final boolean success;
        final UserDTO user;
        final String token;
        final String errorMessage;

        LoginResult(boolean success, UserDTO user, String token, String errorMessage) {
            this.success = success;
            this.user = user;
            this.token = token;
            this.errorMessage = errorMessage;
        }
    }
}