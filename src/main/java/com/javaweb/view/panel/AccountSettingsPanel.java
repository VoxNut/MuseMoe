package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.*;
import com.javaweb.view.HomePage;
import com.javaweb.view.components.AsyncImageLabel;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class AccountSettingsPanel extends JPanel implements ThemeChangeListener {
    // Card names
    private static final String OVERVIEW_CARD = "OVERVIEW";
    private static final String EDIT_PROFILE_CARD = "EDIT PROFILE";
    private static final String CHANGE_PASSWORD_CARD = "CHANGE PASSWORD";
    private static final String CLOSE_ACCOUNT_CARD = "CLOSE ACCOUNT";

    // Main components
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel navigationPanel;

    // Card panels
    private JPanel overviewPanel;
    private JPanel editProfilePanel;
    private JPanel changePasswordPanel;
    private JPanel closeAccountPanel;

    // Theme colors
    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    // Navigation buttons with active indicator
    private Map<String, JButton> navigationButtons = new HashMap<>();
    private String currentCard = OVERVIEW_CARD;

    // User information
    private UserDTO currentUser;

    // Profile edit components
    private JTextField fullNameField;
    private JTextField emailField;
    private JLabel usernameValue;
    private AsyncImageLabel profilePicturePreview;
    private File selectedProfilePicture = null;

    // Password change components
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private MusicPlayerFacade musicPlayerFacade;


    private JLabel warningIcon;
    private JLabel warningTitle;
    private JPanel warningPanel;
    private JLabel warningMessage;
    private JCheckBox confirmCheckbox;


    /**
     * Constructor - initializes the account settings panel
     */
    public AccountSettingsPanel() {
        // Initialize theme colors
        this.backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        this.textColor = ThemeManager.getInstance().getTextColor();
        this.accentColor = ThemeManager.getInstance().getAccentColor();
        musicPlayerFacade = App.getBean(MusicPlayerFacade.class);
        // Get current user
        this.currentUser = CommonApiUtil.fetchCurrentUser();

        initComponents();

        // Register for theme updates
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Create navigation panel
        createNavigationPanel();
        add(navigationPanel, BorderLayout.NORTH);

        // Create main content with CardLayout
        cardLayout = new CardLayout();
        contentPanel = GuiUtil.createPanel(cardLayout);

        // Create cards
        createOverviewPanel();
        createEditProfilePanel();
        createChangePasswordPanel();
        createCloseAccountPanel();

        // Add cards to content panel
        contentPanel.add(overviewPanel, OVERVIEW_CARD);
        contentPanel.add(editProfilePanel, EDIT_PROFILE_CARD);
        contentPanel.add(changePasswordPanel, CHANGE_PASSWORD_CARD);
        contentPanel.add(closeAccountPanel, CLOSE_ACCOUNT_CARD);

        // Create scrollable panel
        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Show initial card
        cardLayout.show(contentPanel, OVERVIEW_CARD);
        updateNavigationButtonStates(OVERVIEW_CARD);
    }

    private void createNavigationPanel() {
        navigationPanel = GuiUtil.createPanel(new MigLayout("fillx, insets 10 20 10 20", "[left][grow, right]", "[]"));
        navigationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                GuiUtil.darkenColor(backgroundColor, 0.1f)));

        JLabel titleLabel = GuiUtil.createLabel("Account Settings", Font.BOLD, 22);
        navigationPanel.add(titleLabel, "left");

        JPanel buttonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // Create navigation buttons
        String[] navItems = {OVERVIEW_CARD, EDIT_PROFILE_CARD, CHANGE_PASSWORD_CARD, CLOSE_ACCOUNT_CARD};

        for (String navItem : navItems) {
            JButton navButton = createNavigationButton(navItem);
            buttonsPanel.add(navButton);
            navigationButtons.put(navItem, navButton);
        }

        navigationPanel.add(buttonsPanel, "right");
    }

    private JButton createNavigationButton(String text) {
        JButton button = GuiUtil.createButton(text);
        GuiUtil.styleButton(button, textColor, backgroundColor, accentColor);
        button.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));

        // Add action to switch cards
        button.addActionListener(e -> {
            cardLayout.show(contentPanel, text);
            currentCard = text;
            updateNavigationButtonStates(text);
        });

        return button;
    }

    private void updateNavigationButtonStates(String activeCard) {
        navigationButtons.forEach((card, button) -> {
            if (card.equals(activeCard)) {
                GuiUtil.styleButton(button, accentColor, textColor, accentColor);
                button.setFont(FontUtil.getSpotifyFont(Font.BOLD, 16));
            } else {
                GuiUtil.styleButton(button, textColor, backgroundColor, accentColor);
                button.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
            }
        });
    }

    private void createOverviewPanel() {
        overviewPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap, insets 30", "[grow, fill]", "[]30[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Account Overview", Font.BOLD, 28);
        overviewPanel.add(headerLabel, "left");

        // Create account info panel
        JPanel accountInfoPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[]", "[]25[]"));
        accountInfoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        // User info container with profile picture
        JPanel userInfoContainer = GuiUtil.createPanel(new MigLayout("fillx", "[center]20[grow]", "[]"));

        // Profile picture panel
        JPanel profilePicturePanel = GuiUtil.createPanel(new BorderLayout());

        AsyncImageLabel profilePicture = GuiUtil.createAsyncImageLabel(150, 150, 15);
        profilePicture.startLoading();
        musicPlayerFacade.populateUserProfile(currentUser, profilePicture::setLoadedImage);
        profilePicturePanel.add(profilePicture, BorderLayout.CENTER);

        userInfoContainer.add(profilePicturePanel, "top");

        // User details panel
        JPanel userDetailsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[][grow]", "[]15[]15[]15[]15[]"));

        // Add a subtle heading
        JLabel infoHeading = GuiUtil.createLabel("Your Account Information", Font.BOLD, 20);
        userDetailsPanel.add(infoHeading, "span 2, gaptop 0, gapbottom 15");

        // Username
        JLabel usernameLabel = GuiUtil.createLabel("Username:", Font.BOLD, 15);
        JLabel usernameValue = GuiUtil.createLabel(currentUser.getUsername(), Font.PLAIN, 15);
        userDetailsPanel.add(usernameLabel);
        userDetailsPanel.add(usernameValue);

        // Full name
        JLabel fullNameLabel = GuiUtil.createLabel("Full Name:", Font.BOLD, 15);
        JLabel fullNameValue = GuiUtil.createLabel(currentUser.getFullName(), Font.PLAIN, 15);
        userDetailsPanel.add(fullNameLabel);
        userDetailsPanel.add(fullNameValue);

        // Email
        JLabel emailLabel = GuiUtil.createLabel("Email:", Font.BOLD, 15);
        JLabel emailValue = GuiUtil.createLabel(currentUser.getEmail(), Font.PLAIN, 15);
        userDetailsPanel.add(emailLabel);
        userDetailsPanel.add(emailValue);

        // Role
        JLabel roleLabel = GuiUtil.createLabel("Account Type:", Font.BOLD, 15);
        String roleText = currentUser.determineUserRole();
        JLabel roleValue = GuiUtil.createLabel(roleText, Font.PLAIN, 15);
        userDetailsPanel.add(roleLabel);
        userDetailsPanel.add(roleValue);

        // Join date
        JLabel joinDateLabel = GuiUtil.createLabel("Joined:", Font.BOLD, 15);
        String joinDate = "N/A";
        if (currentUser.getCreatedDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
            joinDate = dateFormat.format(currentUser.getCreatedDate());
        }
        JLabel joinDateValue = GuiUtil.createLabel(joinDate, Font.PLAIN, 15);
        userDetailsPanel.add(joinDateLabel);
        userDetailsPanel.add(joinDateValue);

        userInfoContainer.add(userDetailsPanel, "grow");
        accountInfoPanel.add(userInfoContainer, "grow");

        // Add quick links for editing
        JPanel quickLinksPanel = GuiUtil.createPanel(new MigLayout("", "push[]20[]push", "[]"));

        JButton editProfileButton = GuiUtil.createButton("Edit Profile");
        editProfileButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        editProfileButton.addActionListener(e -> {
            cardLayout.show(contentPanel, EDIT_PROFILE_CARD);
            currentCard = EDIT_PROFILE_CARD;
            updateNavigationButtonStates(EDIT_PROFILE_CARD);
        });

        JButton changePasswordButton = GuiUtil.createButton("Change Password");
        changePasswordButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        changePasswordButton.addActionListener(e -> {
            cardLayout.show(contentPanel, CHANGE_PASSWORD_CARD);
            currentCard = CHANGE_PASSWORD_CARD;
            updateNavigationButtonStates(CHANGE_PASSWORD_CARD);
        });

        quickLinksPanel.add(editProfileButton);
        quickLinksPanel.add(changePasswordButton);

        accountInfoPanel.add(quickLinksPanel, "grow, gaptop 15");

        overviewPanel.add(accountInfoPanel, "grow");
    }

    private void createEditProfilePanel() {
        editProfilePanel = GuiUtil.createPanel(new MigLayout("fillx, wrap, insets 30", "[grow, fill]", "[]30[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Edit Profile", Font.BOLD, 28);
        editProfilePanel.add(headerLabel, "left");

        // Create form panel
        JPanel formPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow]", "[]30[]"));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        // Add a subtle heading
        JLabel formHeading = GuiUtil.createLabel("Update Your Profile Information", Font.BOLD, 20);
        formPanel.add(formHeading, "gapbottom 15");

        JPanel contentPanel = GuiUtil.createPanel(new MigLayout("fillx", "[center, 200]20[grow]", "[]"));

        // Profile picture panel
        JPanel picturePanel = GuiUtil.createPanel(new MigLayout("wrap", "[center]", "[]15[]"));

        profilePicturePreview = GuiUtil.createAsyncImageLabel(150, 150, 15);
        profilePicturePreview.startLoading();
        musicPlayerFacade.populateUserProfile(currentUser, profilePicturePreview::setLoadedImage);

        JPanel pictureButtonPanel = GuiUtil.createPanel(new BorderLayout());

        JButton selectPictureButton = GuiUtil.createButton("Change Profile Picture");
        selectPictureButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 13));
        selectPictureButton.addActionListener(e -> selectProfilePicture());

        pictureButtonPanel.add(selectPictureButton, BorderLayout.CENTER);
        picturePanel.add(profilePicturePreview, "center");
        picturePanel.add(pictureButtonPanel, "center, width 180!");

        // Form fields panel
        JPanel fieldsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[][grow]", "[]20[]20[]"));

        // Username (non-editable)
        JLabel usernameLabel = GuiUtil.createLabel("Username:", Font.BOLD, 15);
        usernameValue = GuiUtil.createLabel(currentUser.getUsername(), Font.PLAIN, 15);
        fieldsPanel.add(usernameLabel, "");
        fieldsPanel.add(usernameValue, "growx");

        // Full name
        JLabel fullNameLabel = GuiUtil.createLabel("Full Name:", Font.BOLD, 15);
        fullNameField = GuiUtil.createTextField(currentUser.getFullName(), 30);
        fullNameField.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 15));
        fullNameField.setPreferredSize(new Dimension(300, 35));
        fieldsPanel.add(fullNameLabel, "");
        fieldsPanel.add(fullNameField, "growx");

        // Email
        JLabel emailLabel = GuiUtil.createLabel("Email:", Font.BOLD, 15);
        emailField = GuiUtil.createTextField(currentUser.getEmail(), 30);
        emailField.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 15));
        emailField.setPreferredSize(new Dimension(300, 35));
        fieldsPanel.add(emailLabel, "");
        fieldsPanel.add(emailField, "growx");

        contentPanel.add(picturePanel, "top");
        contentPanel.add(fieldsPanel, "grow");

        formPanel.add(contentPanel, "grow");

        // Save button container
        JPanel buttonPanel = GuiUtil.createPanel(new MigLayout("", "push[]push", "[]"));

        JButton saveButton = GuiUtil.createButton("Save Changes");
        saveButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 15));
        saveButton.addActionListener(e -> saveProfileChanges());

        buttonPanel.add(saveButton, "center");
        formPanel.add(buttonPanel, "grow, gaptop 20");

        editProfilePanel.add(formPanel, "grow");
    }

    private void createChangePasswordPanel() {
        changePasswordPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap, insets 30", "[grow, fill]", "[]30[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Change Password", Font.BOLD, 28);
        changePasswordPanel.add(headerLabel, "left");

        // Create form panel
        JPanel formPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[]", "[]30[]20[]"));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        // Add a subtle heading
        JLabel formHeading = GuiUtil.createLabel("Update Your Password", Font.BOLD, 20);
        formPanel.add(formHeading, "gapbottom 15");

        // Password fields
        JPanel passwordFieldsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[][grow]", "[]20[]20[]"));

        // Current password
        JLabel currentPasswordLabel = GuiUtil.createLabel("Current Password:", Font.BOLD, 15);
        currentPasswordField = GuiUtil.createPasswordField(30);
        currentPasswordField.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 15));
        currentPasswordField.setPreferredSize(new Dimension(300, 35));
        passwordFieldsPanel.add(currentPasswordLabel, "");
        passwordFieldsPanel.add(currentPasswordField, "growx");

        // New password
        JLabel newPasswordLabel = GuiUtil.createLabel("New Password:", Font.BOLD, 15);
        newPasswordField = GuiUtil.createPasswordField(30);
        newPasswordField.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 15));
        newPasswordField.setPreferredSize(new Dimension(300, 35));
        passwordFieldsPanel.add(newPasswordLabel, "");
        passwordFieldsPanel.add(newPasswordField, "growx");

        // Confirm new password
        JLabel confirmPasswordLabel = GuiUtil.createLabel("Confirm New Password:", Font.BOLD, 15);
        confirmPasswordField = GuiUtil.createPasswordField(30);
        confirmPasswordField.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 15));
        confirmPasswordField.setPreferredSize(new Dimension(300, 35));
        passwordFieldsPanel.add(confirmPasswordLabel, "");
        passwordFieldsPanel.add(confirmPasswordField, "growx");

        formPanel.add(passwordFieldsPanel, "grow");

        // Password requirements info in a nicer panel
        JPanel requirementsPanel = GuiUtil.createPanel(new BorderLayout(10, 10));
        requirementsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.15f), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel iconPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel infoIcon = GuiUtil.createIconLabel(AppConstant.INFORMATION_ICON_PATH, 24, textColor);
        iconPanel.add(infoIcon, BorderLayout.NORTH);

        JPanel textPanel = GuiUtil.createPanel(new BorderLayout());

        JLabel passwordRequirementsLabel = GuiUtil.createLabel(
                "<html>Password must contain at least 8 characters, including:<br>" +
                        "• One uppercase letter<br>" +
                        "• One lowercase letter<br>" +
                        "• One number<br>" +
                        "• One special character</html>",
                Font.ITALIC, 13);
        passwordRequirementsLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));
        textPanel.add(passwordRequirementsLabel, BorderLayout.CENTER);

        requirementsPanel.add(iconPanel, BorderLayout.WEST);
        requirementsPanel.add(textPanel, BorderLayout.CENTER);

        formPanel.add(requirementsPanel, "grow");

        // Save button container
        JPanel buttonPanel = GuiUtil.createPanel(new MigLayout("", "push[]push", "[]"));

        JButton saveButton = GuiUtil.createButton("Change Password");
        saveButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 15));
        saveButton.addActionListener(e -> changePassword());

        buttonPanel.add(saveButton, "center");
        formPanel.add(buttonPanel, "grow, gaptop 20");

        changePasswordPanel.add(formPanel, "grow");
    }

    private void createCloseAccountPanel() {
        closeAccountPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap, insets 20", "[grow, fill]", "[]20[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Close Account", Font.BOLD, 24);
        closeAccountPanel.add(headerLabel, "left");

        // Warning panel
        warningPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[]", "[]15[]15[]"));
        warningPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Warning icon and title
        JPanel headerPanel = GuiUtil.createPanel(new MigLayout("insets 0", "[]10[]", "[]"));

        warningIcon = GuiUtil.createIconLabel(AppConstant.WARNING_ICON_PATH, 32, accentColor);
        warningTitle = GuiUtil.createLabel("Warning: Account Closure is Permanent", Font.BOLD, 18);
        warningTitle.setForeground(accentColor);

        headerPanel.add(warningIcon);
        headerPanel.add(warningTitle);
        warningPanel.add(headerPanel);

        // Warning message
        warningMessage = GuiUtil.createLabel(
                "<html><div style='width: 450px;'>" +
                        "Closing your account will permanently delete all your data including:" +
                        "<ul>" +
                        "<li>Your profile and personal information</li>" +
                        "<li>Your playlists and liked songs</li>" +
                        "<li>Your listening history and statistics</li>" +
                        "</ul>" +
                        "This action <b>cannot be undone</b>. You will need to create a new account if you wish to use MuseMoe again." +
                        "</div></html>",
                Font.PLAIN, 14);
        warningMessage.setForeground(GuiUtil.darkenColor(textColor, 0.3f));
        warningPanel.add(warningMessage);

        // Confirmation checkbox
        confirmCheckbox = new JCheckBox("I understand that closing my account is permanent and cannot be undone");
        confirmCheckbox.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
        confirmCheckbox.setOpaque(false);
        confirmCheckbox.setForeground(accentColor);

        warningPanel.add(confirmCheckbox);

        closeAccountPanel.add(warningPanel);

        // Close account button
        JButton closeAccountButton = GuiUtil.createButton("Close Account");
        closeAccountButton.setEnabled(false);

        confirmCheckbox.addActionListener(e -> closeAccountButton.setEnabled(confirmCheckbox.isSelected()));

        closeAccountButton.addActionListener(e -> closeAccount());

        closeAccountPanel.add(closeAccountButton, "center");
    }


    private void selectProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png"));
        fileChooser.setCurrentDirectory(AppConstant.USER_AVATAR_DIRECTORY);
        fileChooser.setPreferredSize(AppConstant.FILE_CHOOSER_SIZE);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                selectedProfilePicture = fileChooser.getSelectedFile();
                BufferedImage image = ImageIO.read(selectedProfilePicture);
                if (image != null) {
                    profilePicturePreview.setLoadedImage(image);
                    GuiUtil.showToast(this, "Profile picture selected. Don't forget to save changes!");
                } else {
                    GuiUtil.showErrorMessageDialog(this, "Selected file is not a valid image.");
                    selectedProfilePicture = null;
                }
            } catch (Exception e) {
                log.error("Error loading profile picture", e);
                GuiUtil.showErrorMessageDialog(this, "Error loading image: " + e.getMessage());
                selectedProfilePicture = null;
            }
        }
    }

    private void saveProfileChanges() {
        try {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();

            // Validate inputs
            if (fullName.isEmpty()) {
                GuiUtil.showWarningMessageDialog(this, "Full name cannot be empty.");
                return;
            }

            if (email.isEmpty() || !ValidateUtil.isValidEmail(email)) {
                GuiUtil.showWarningMessageDialog(this, "Please enter a valid email address.");
                return;
            }

            if (!currentUser.getEmail().equals(email) && CommonApiUtil.fetchUserByEmail(email) != null) {
                GuiUtil.showWarningMessageDialog(this, "email registered, please choose another email!");
                return;
            }

            // Prepare profile picture if selected
            MockMultipartFile profilePicture = null;
            if (selectedProfilePicture != null) {
                try {
                    profilePicture = new MockMultipartFile(
                            selectedProfilePicture.getName(),
                            selectedProfilePicture.getName(),
                            Files.probeContentType(selectedProfilePicture.toPath()),
                            Files.readAllBytes(selectedProfilePicture.toPath())
                    );
                } catch (Exception e) {
                    log.error("Error preparing profile picture", e);
                    GuiUtil.showErrorMessageDialog(this, "Error processing image: " + e.getMessage());
                    return;
                }
            }

            // Update user profile
            boolean success = CommonApiUtil.updateUserProfile(fullName, email, profilePicture);

            if (success) {
                // Update the current user object with new information
                currentUser.setFullName(fullName);
                currentUser.setEmail(email);

                HomePage homepage = GuiUtil.findHomePageInstance(this);
                homepage.setFullNameLabel(currentUser.getFullName());
                homepage.refreshUserAvatar();

                // Update session
                UserSessionManager.getInstance().updateUserInfo(currentUser);

                GuiUtil.showSuccessMessageDialog(this, "Profile updated successfully!");

                // Reset selected profile picture
                selectedProfilePicture = null;


                // Refresh the overview panel
                createOverviewPanel();
                contentPanel.remove(overviewPanel);
                contentPanel.add(overviewPanel, OVERVIEW_CARD);

            } else {
                GuiUtil.showErrorMessageDialog(this, "Failed to update profile. Please try again.");
            }
        } catch (Exception e) {
            log.error("Error saving profile changes", e);
            GuiUtil.showErrorMessageDialog(this, "An error occurred: " + e.getMessage());
        }
    }

    private void changePassword() {
        try {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Validate inputs
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                GuiUtil.showWarningMessageDialog(this, "All password fields are required.");
                return;
            }

            if (CommonApiUtil.checkCurrentPassword(currentPassword) == false) {
                GuiUtil.showWarningMessageDialog(this, "Current password is incorrect.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                GuiUtil.showWarningMessageDialog(this, "New passwords do not match.");
                return;
            }

            if (!ValidateUtil.isValidPassword(newPassword)) {
                GuiUtil.showWarningMessageDialog(this,
                        "Password must contain at least 8 characters, including uppercase, " +
                                "lowercase, number, and special character.");
                return;
            }


            // Verify current password and update to new password
            boolean success = CommonApiUtil.changeUserPassword(newPassword);

            if (success) {
                // Clear password fields
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");

                GuiUtil.showSuccessMessageDialog(this, "Password changed successfully!");
            } else {
                GuiUtil.showErrorMessageDialog(this, "Failed to change password. Please verify your current password and try again.");
            }
        } catch (Exception e) {
            log.error("Error changing password", e);
            GuiUtil.showErrorMessageDialog(this, "An error occurred: " + e.getMessage());
        }
    }

    private void closeAccount() {
        // Show final confirmation dialog
        int option = GuiUtil.showConfirmMessageDialog(
                this,
                "Are you absolutely sure you want to close your account?\nThis action CANNOT be undone.",
                "Final Confirmation"
        );

        if (option == JOptionPane.YES_OPTION) {
            try {
                // Call API to close account
                boolean success = CommonApiUtil.closeUserAccount();

                if (success) {
                    GuiUtil.showInfoMessageDialog(this, "Your account has been closed. The application will now exit.");

                    // Clear any stored tokens
                    TokenStorage.clearToken();

                    // Exit application
                    System.exit(0);
                } else {
                    GuiUtil.showErrorMessageDialog(this, "Failed to close account. Please try again later.");
                }
            } catch (Exception e) {
                log.error("Error closing account", e);
                GuiUtil.showErrorMessageDialog(this, "An error occurred: " + e.getMessage());
            }
        }
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        // Update navigation button states
        updateNavigationButtonStates(currentCard);


        warningPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        GuiUtil.changeLabelIconColor(warningIcon, accentColor);

        warningTitle.setForeground(accentColor);

        warningMessage.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

        confirmCheckbox.setForeground(accentColor);
        // Repaint the entire panel
        repaint();
    }
}