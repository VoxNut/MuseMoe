package test.SwingTest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.javaweb.constant.AppConstant;
import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class MusicPlayerSwingApp extends JFrame {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = AppConstant.CREDENTIALS_FILE_PATH;
    private static final String APPLICATION_NAME = "MusicPlayerSwingApp";
    private static final String TARGET_FOLDER_ID = "1dpEn3AUPZbA9Yn1c5Hs6Dfz429GoP8lV";
    private static final Logger LOGGER = Logger.getLogger(MusicPlayerSwingApp.class.getName());

    private JTextField filePathField;
    private JTextField titleField;
    private JTextField artistField;
    private JTextField durationField;
    private JTextField urlField;
    private JButton selectFileButton;
    private JButton uploadButton;
    private JButton playButton;
    private Drive driveService;
    private java.io.File selectedFile;
    private String uploadedFileUrl;
    private Player audioPlayer;

    public MusicPlayerSwingApp() {
        super("Music Player Upload Demo");
        initializeDriveService();
        initComponents();
        layoutComponents();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    private void initializeDriveService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            JOptionPane.showMessageDialog(this, "Failed to initialize Google Drive: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initComponents() {
        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        titleField = new JTextField(30);
        titleField.setEditable(false);
        artistField = new JTextField(30);
        artistField.setEditable(false);
        durationField = new JTextField(30);
        durationField.setEditable(false);
        urlField = new JTextField(30);
        urlField.setEditable(false);

        selectFileButton = new JButton("Select MP3 File");
        uploadButton = new JButton("Upload & Extract Metadata");
        playButton = new JButton("Play Song");
        playButton.setEnabled(false);

        selectFileButton.addActionListener(e -> selectMp3File());
        uploadButton.addActionListener(e -> uploadAndExtractMetadata());
        playButton.addActionListener(e -> playSong());
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("MP3 File:"), gbc);
        gbc.gridx = 1;
        panel.add(filePathField, gbc);
        gbc.gridx = 2;
        panel.add(selectFileButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Artist:"), gbc);
        gbc.gridx = 1;
        panel.add(artistField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Duration (seconds):"), gbc);
        gbc.gridx = 1;
        panel.add(durationField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Google Drive URL:"), gbc);
        gbc.gridx = 1;
        panel.add(urlField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(uploadButton, gbc);
        gbc.gridx = 1;
        panel.add(playButton, gbc);

        add(panel, BorderLayout.CENTER);
    }

    private void selectMp3File() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new java.io.File("src/main/java/com/javaweb/view/mini_musicplayer/audio"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MP3 Files", "mp3"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            uploadButton.setEnabled(true);
        }
    }

    private void uploadAndExtractMetadata() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select an MP3 file first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Extract metadata using JAudioTagger (assuming it's available)
            org.jaudiotagger.audio.AudioFile audioFile = org.jaudiotagger.audio.AudioFileIO.read(selectedFile);
            org.jaudiotagger.tag.Tag tag = audioFile.getTag();
            String title = tag.getFirst(org.jaudiotagger.tag.FieldKey.TITLE);
            String artist = tag.getFirst(org.jaudiotagger.tag.FieldKey.ARTIST);
            int duration = audioFile.getAudioHeader().getTrackLength();

            // Upload to Google Drive in the specified folder
            File fileMetadata = new File();
            fileMetadata.setName(selectedFile.getName());
            fileMetadata.setParents(Collections.singletonList(TARGET_FOLDER_ID));
            FileContent mediaContent = new FileContent("audio/mpeg", selectedFile);
            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, webContentLink")
                    .execute();
            uploadedFileUrl = uploadedFile.getWebContentLink();

            // Update GUI
            titleField.setText(title);
            artistField.setText(artist);
            durationField.setText(String.valueOf(duration));
            urlField.setText(uploadedFileUrl);
            playButton.setEnabled(true);

            JOptionPane.showMessageDialog(this, "Upload successful to folder! Metadata extracted.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            LOGGER.severe("Upload error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Upload error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void playSong() {
        if (uploadedFileUrl == null) {
            JOptionPane.showMessageDialog(this, "No song uploaded to play.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (audioPlayer != null) {
                audioPlayer.close();
                audioPlayer = null;
            }

            // Use HttpURLConnection to handle redirects and get the raw stream
            URL url = new URL(uploadedFileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + getCredentials(GoogleNetHttpTransport.newTrustedTransport()).getAccessToken());
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            // Check response
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error: " + responseCode + " - " + connection.getResponseMessage());
            }

            // Stream the MP3
            InputStream inputStream = connection.getInputStream();
            audioPlayer = new Player(inputStream);
            new Thread(() -> {
                try {
                    audioPlayer.play();
                    LOGGER.info("Playback completed successfully");
                } catch (Exception e) {
                    LOGGER.severe("Playback error: " + e.getMessage());
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Playback error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                    );
                } finally {
                    if (audioPlayer != null) {
                        audioPlayer.close();
                    }
                    try {
                        inputStream.close();
                        connection.disconnect();
                    } catch (IOException e) {
                        LOGGER.warning("Failed to close stream or connection: " + e.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {
            LOGGER.severe("Unexpected playback error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Unexpected playback error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8081).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MusicPlayerSwingApp().setVisible(true));
    }
}