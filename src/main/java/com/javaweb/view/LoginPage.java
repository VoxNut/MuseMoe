package com.javaweb.view;

import com.javaweb.constant.AppConstant;
import com.javaweb.enums.AccountStatus;
import com.javaweb.model.dto.RoleDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.*;
import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private JLabel statusLabel;
    private JLabel forgotPasswordLabel;

    public LoginPage() {
        initializeFrame();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(createMainPanel(), "login");
        mainPanel.add(createSignUpPanel(), "signup");
        add(mainPanel);
        addEventListeners();
        GuiUtil.applyWindowStyle(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            UIManager.put("TitlePane.iconSize", new Dimension(20, 20));
            loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_ICON_PATH, 100, 100).getImage());
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
        JLabel signUpTitle = new JLabel("REGISTER", SwingConstants.CENTER);
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
        JButton signUpButton = createButton("Register");
        signUpButton.addActionListener(e -> handleSignUp(usernameField.getText(), emailField.getText(),
                new String(passwordField.getPassword())));
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

        // Create overlay panel for the register button
        JPanel overlayPanel = new JPanel();
        overlayPanel.setOpaque(false);
        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));

        // 'Sign up' button
        JButton signUpButton = createButton("<html><div style=\"text-align: center;\">Haven't got an account yet?<br>REGISTER NOW!</div></html>");
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
        JButton signInButton = createButton("Login");
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

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(layeredPane, BorderLayout.CENTER);

        return containerPanel;
    }

    private void handleSignUp(String username, String email, String password) {
        if (!validateInputFields(username, email, password)) {
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


        if (CommonApiUtil.createNewUser(username, password, email)) {
            GuiUtil.showSuccessMessageDialog(this, "Create account successfully. You can log in now! :)");
            cardLayout.show(mainPanel, "login");
        } else {
            GuiUtil.showErrorMessageDialog(this, "An error has occurred when creating account! ");
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
        JLabel loginTitle = new JLabel("LOGIN", SwingConstants.CENTER);
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
        loginButton = createButton("Login");
        exitButton = createButton("Exit");
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
        forgotPasswordLabel = new JLabel("Forgot your password?");
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
            GuiUtil.showWarningMessageDialog(this, "Pleas enter Username!.");
            return;
        }


        UserDTO user = CommonApiUtil.fetchUserByUsername(username);

        String userEmail = user.getEmail();

        // Generate a temporary password
        String tempPassword = generateTemporaryPassword();

        // Update the user's password in the database
        if (updatePasswordInDatabase(user.getId(), tempPassword)) {
            // Send email with the temporary password
            SendEmailUtil.sendEmail(userEmail, tempPassword);
            GuiUtil.showInfomationMessageDialog(this, "An email with instruction have been sending to you.");
        } else {
            GuiUtil.showErrorMessageDialog(this, "An error has occurred!");
        }


    }

    // Method to generate a temporary password
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean updatePasswordInDatabase(Long userId, String tempPassword) {
        return CommonApiUtil.updateUserPassword(userId, tempPassword);
    }

    private boolean validateInputFields(String username, String email, String password) {
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
        if (!ValidateUtil.isValidPassword(password)) {
            GuiUtil.showWarningMessageDialog(this, "Password not valid.");
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
                CommonApiUtil.updateLastLoginTime();
                UserDTO user = CommonApiUtil.fetchUserByUsername(username);
                if (user.getAccountStatus().equals(AccountStatus.INACTIVE)) {
                    statusLabel.setText("User not existed or deleted");
                    response.close();
                }
                String avatarLink;
                if (user.getAvatar() != null) {
                    avatarLink = user.getAvatar().getFileUrl();
                } else {
                    avatarLink = "";
                }
                String userFullName = user.getFullName();
                Set<String> roles = new HashSet<>();
                for (RoleDTO role : user.getRoles()) {
                    roles.add("ROLE_" + role.getCode());
                }
                this.dispose();
                HomePage homePage = new HomePage(avatarLink, userFullName, roles, user);
                UIManager.put("TitlePane.iconSize", new Dimension(24, 24));
                homePage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_ICON_PATH, 512, 512).getImage());
                homePage.setVisible(true);
            } else {
                // Authentication failed
                statusLabel.setText("Username or Password incorrect!");
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("An error has occurred!");
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