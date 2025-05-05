package com.javaweb.service.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.javaweb.constant.AppConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveService {
    private static final String APPLICATION_NAME = "MuseMoe";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = AppConstant.TOKENS_DIRECTORY_PATH;
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = AppConstant.CREDENTIALS_FILE_PATH;

    // Define your folder IDs as constants
    public static final String MUSIC_FOLDER_ID = "1dpEn3AUPZbA9Yn1c5Hs6Dfz429GoP8lV";
    public static final String IMAGE_FOLDER_ID = "1ElAlsQHLqUa58DO1mjegwATX7gLlzQNR";


    public static final String ALBUM_COVER_FOLDER_ID = "1JZAj1ayeGdPEa1qCMc7-DwWhImAro0UG";
    public static final String ARTIST_PROFILE_FOLDER_ID = "1SQwFXr9FF77FJrzJqM2xQKyXsAJyNUal";
    public static final String AVATAR_FOLDER_ID = "1JVXC0zWLIpZ2uT8Lhn4A1qdv6pMUAU1l";
    public static final String BACKGROUND_FOLDER_ID = "10tYyJdL-7jMXmoY4g_0gVgD34K1D2W58";
    public static final String ICON_FOLDER_ID = "1ztey0ZaLuNJwCgLxwlkbZvvdVA7uKjYc";
    public static final String LOGO_FOLDER_ID = "1-aOEbgjHuB-_9-dYeTD2dpLwjROlZKbB";
    public static final String MISC_FOLDER_ID = "1cG_2LxVDNGK3KeMgg2nzoUUKBTvepERI";


    private final Drive driveService;


    public GoogleDriveService() throws IOException, GeneralSecurityException {
        final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(HttpTransport HTTP_TRANSPORT) throws IOException {

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public List<File> listMusicFilesFromFolder() {
        try {
            String query = String.format(
                    "'%s' in parents and mimeType contains 'audio/' and trashed=false",
                    MUSIC_FOLDER_ID);

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, webContentLink, description, properties)")
                    .execute();

            log.info("Found {} music files in the specified folder", result.getFiles().size());
            return result.getFiles();
        } catch (IOException e) {
            log.error("Failed to list music files from folder: {}", MUSIC_FOLDER_ID, e);
            return Collections.emptyList();
        }
    }

    public List<File> listImageFilesFromFolder() {
        try {
            String query = String.format(
                    "'%s' in parents and (mimeType contains 'image/' or mimeType contains 'jpeg' or mimeType contains 'png') and trashed=false",
                    IMAGE_FOLDER_ID);

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, webContentLink, thumbnailLink)")
                    .execute();

            log.info("Found {} image files in the specified folder", result.getFiles().size());
            return result.getFiles();
        } catch (IOException e) {
            log.error("Failed to list image files from folder: {}", IMAGE_FOLDER_ID, e);
            return Collections.emptyList();
        }
    }


    public InputStream getFileContent(String fileId) throws IOException {
        return driveService.files().get(fileId)
                .executeMediaAsInputStream();
    }

    public File getFileMetadata(String fileId) throws IOException {
        return driveService.files().get(fileId)
                .setFields("id, name, mimeType, size, webContentLink")
                .execute();
    }

    public File getImageByName(String imageName) {
        try {
            String query = String.format(
                    "'%s' in parents and name = '%s' and trashed=false",
                    IMAGE_FOLDER_ID, imageName.replace("'", "\\'"));

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, webContentLink, thumbnailLink)")
                    .execute();

            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0);
            }
            return null;
        } catch (IOException e) {
            log.error("Failed to find image by name: {}", imageName, e);
            return null;
        }
    }


    public File getMusicByName(String songName) {
        try {
            String query = String.format(
                    "'%s' in parents and name = '%s' and trashed=false",
                    MUSIC_FOLDER_ID, songName.replace("'", "\\'"));

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, webContentLink)")
                    .execute();

            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0);
            }
            return null;
        } catch (IOException e) {
            log.error("Failed to find song by name: {}", songName, e);
            return null;
        }
    }

    public File findMatchingAlbumArt(String songName) {
        try {
            // First, try exact match without extension
            String nameWithoutExt = songName;
            if (nameWithoutExt.lastIndexOf('.') > 0) {
                nameWithoutExt = nameWithoutExt.substring(0, nameWithoutExt.lastIndexOf('.'));
            }

            // 1. Try exact match first in album covers folder (priority)
            File exactMatch = getAlbumCoverByName(nameWithoutExt + ".jpg");
            if (exactMatch != null) return exactMatch;

            exactMatch = getAlbumCoverByName(nameWithoutExt + ".png");
            if (exactMatch != null) return exactMatch;

            // 2. Try to find any image containing the song name in album covers folder
            String query = String.format(
                    "'%s' in parents and name contains '%s' and (mimeType contains 'image/' or mimeType contains 'jpeg' or mimeType contains 'png' or mimeType contains 'jpg') and trashed=false",
                    ALBUM_COVER_FOLDER_ID, nameWithoutExt.replace("'", "\\'"));

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, webContentLink, thumbnailLink)")
                    .execute();

            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0);
            }

            // 3. If not found in album covers, look in the general images folder
            exactMatch = getImageByName(nameWithoutExt + ".jpg");
            if (exactMatch != null) return exactMatch;

            exactMatch = getImageByName(nameWithoutExt + ".png");
            if (exactMatch != null) return exactMatch;

            // 4. Try to find any image containing the song name in general folder
            query = String.format(
                    "'%s' in parents and name contains '%s' and (mimeType contains 'image/' or mimeType contains 'jpeg' or mimeType contains 'png' or mimeType contains 'jpg') and trashed=false",
                    IMAGE_FOLDER_ID, nameWithoutExt.replace("'", "\\'"));

            result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, webContentLink, thumbnailLink)")
                    .execute();

            files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0);
            }

            // If no match, return null
            return null;
        } catch (IOException e) {
            log.error("Failed to find matching album art for song: {}", songName, e);
            return null;
        }
    }

    public List<DriveFileBundle> loadAllSongsWithMetadata() {
        // Get all music files
        List<File> musicFiles = listMusicFilesFromFolder();

        // Create a list of bundles (song + album art)
        return musicFiles.stream()
                .map(musicFile -> {
                    DriveFileBundle bundle = new DriveFileBundle();
                    bundle.setSongFile(musicFile);

                    // Find matching album art
                    File albumArt = findMatchingAlbumArt(musicFile.getName());
                    if (albumArt != null) {
                        bundle.setAlbumArtFile(albumArt);
                    }

                    return bundle;
                })
                .collect(Collectors.toList());
    }

    public static class DriveFileBundle {
        private File songFile;
        private File albumArtFile;

        public DriveFileBundle() {
        }

        public File getSongFile() {
            return songFile;
        }

        public void setSongFile(File songFile) {
            this.songFile = songFile;
        }

        public File getAlbumArtFile() {
            return albumArtFile;
        }

        public void setAlbumArtFile(File albumArtFile) {
            this.albumArtFile = albumArtFile;
        }

        public boolean hasAlbumArt() {
            return albumArtFile != null;
        }
    }


    public List<File> listAlbumCoverImages() {
        return listImageFilesFromSpecificFolder(ALBUM_COVER_FOLDER_ID);
    }


    public List<File> listArtistProfileImages() {
        return listImageFilesFromSpecificFolder(ARTIST_PROFILE_FOLDER_ID);
    }


    public List<File> listAvatarImages() {
        return listImageFilesFromSpecificFolder(AVATAR_FOLDER_ID);
    }


    public List<File> listBackgroundImages() {
        return listImageFilesFromSpecificFolder(BACKGROUND_FOLDER_ID);
    }


    public List<File> listIconImages() {
        return listImageFilesFromSpecificFolder(ICON_FOLDER_ID);
    }


    public List<File> listLogoImages() {
        return listImageFilesFromSpecificFolder(LOGO_FOLDER_ID);
    }


    public List<File> listMiscellaneousImages() {
        return listImageFilesFromSpecificFolder(MISC_FOLDER_ID);
    }

    /**
     * Generic method to list image files from a specific folder
     *
     * @param folderId The ID of the folder to list files from
     * @return List of image files
     */
    private List<File> listImageFilesFromSpecificFolder(String folderId) {
        try {
            String query = String.format(
                    "'%s' in parents and (mimeType contains 'image/' or mimeType contains 'jpeg' or mimeType contains 'png' or mimeType contains 'jpg') and trashed=false",
                    folderId);

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, webContentLink, thumbnailLink)")
                    .execute();

            log.info("Found {} image files in folder: {}", result.getFiles().size(), folderId);
            return result.getFiles();
        } catch (IOException e) {
            log.error("Failed to list image files from folder: {}", folderId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets an album cover image by name
     *
     * @param imageName Name of the image to find
     * @return File object or null if not found
     */
    public File getAlbumCoverByName(String imageName) {
        return getImageFromSpecificFolder(ALBUM_COVER_FOLDER_ID, imageName);
    }


    public BufferedImage getBufferImage(String googleDriveImageId) {
        try {
            InputStream inputStream = getFileContent(googleDriveImageId);
            if (inputStream == null) {
                return null;
            }

            byte[] imageData = IOUtils.toByteArray(inputStream);
            inputStream.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(byteArrayInputStream);
            byteArrayInputStream.close();

            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public File getArtistProfileByName(String imageName) {
        return getImageFromSpecificFolder(ARTIST_PROFILE_FOLDER_ID, imageName);
    }

    public File getAvatarByName(String imageName) {
        return getImageFromSpecificFolder(AVATAR_FOLDER_ID, imageName);
    }

    public File getBackgroundByName(String imageName) {
        return getImageFromSpecificFolder(BACKGROUND_FOLDER_ID, imageName);
    }


    public File getIconByName(String imageName) {
        return getImageFromSpecificFolder(ICON_FOLDER_ID, imageName);
    }


    public File getLogoByName(String imageName) {
        return getImageFromSpecificFolder(LOGO_FOLDER_ID, imageName);
    }


    public File getMiscImageByName(String imageName) {
        return getImageFromSpecificFolder(MISC_FOLDER_ID, imageName);
    }

    public String uploadImageFile(MultipartFile file, String folderId) throws IOException {
        // Create file metadata
        File fileMetadata = new File();
        fileMetadata.setName(generateUniqueFilename(file.getOriginalFilename()));
        fileMetadata.setParents(Collections.singletonList(folderId));

        // Convert MultipartFile to InputStreamContent
        java.io.File tempFile = java.io.File.createTempFile("upload-", ".tmp");
        file.transferTo(tempFile);
        FileContent mediaContent = new FileContent(file.getContentType(), tempFile);

        try {
            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, mimeType, size, webContentLink")
                    .execute();
            log.info("File uploaded to Google Drive: {}", uploadedFile.getName());
            return uploadedFile.getId();
        } finally {
            // Clean up temporary file
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }


    private String generateUniqueFilename(String originalFilename) {
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            originalFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        }

        return timestamp + "-" + originalFilename + extension;
    }


    private File getImageFromSpecificFolder(String folderId, String imageName) {
        try {
            String query = String.format(
                    "'%s' in parents and name = '%s' and trashed=false",
                    folderId, imageName.replace("'", "\\'"));

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, webContentLink, thumbnailLink)")
                    .execute();

            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0);
            }
            return null;
        } catch (IOException e) {
            log.error("Failed to find image '{}' in folder: {}", imageName, folderId, e);
            return null;
        }
    }


    public File findImageAcrossAllFolders(String imageName) {
        File image = getAlbumCoverByName(imageName);
        if (image != null) return image;

        image = getArtistProfileByName(imageName);
        if (image != null) return image;

        image = getAvatarByName(imageName);
        if (image != null) return image;

        image = getBackgroundByName(imageName);
        if (image != null) return image;

        image = getIconByName(imageName);
        if (image != null) return image;

        image = getLogoByName(imageName);
        if (image != null) return image;

        image = getMiscImageByName(imageName);
        if (image != null) return image;

        // Check main images folder as last resort
        return getImageByName(imageName);
    }

    public Map<String, List<File>> getAllImagesAcrossFolders() {
        Map<String, List<File>> allImages = new HashMap<>();

        allImages.put("album_cover", listAlbumCoverImages());
        allImages.put("artist_profile", listArtistProfileImages());
        allImages.put("avatar", listAvatarImages());
        allImages.put("background", listBackgroundImages());
        allImages.put("icon", listIconImages());
        allImages.put("logo", listLogoImages());
        allImages.put("miscellaneous", listMiscellaneousImages());
        allImages.put("root", listImageFilesFromFolder());

        return allImages;
    }

    public String uploadSongFile(MultipartFile file) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(MUSIC_FOLDER_ID));

        java.io.File tempFile = java.io.File.createTempFile("upload-", ".tmp");
        file.transferTo(tempFile);
        FileContent mediaContent = new FileContent(file.getContentType(), tempFile);

        try {
            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, mimeType, size, webContentLink")
                    .execute();

            log.info("File uploaded to Google Drive: {}", uploadedFile.getName());
            return uploadedFile.getId();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public List<Map<String, String>> uploadMultipleSongFiles(List<MultipartFile> files) {
        List<Map<String, String>> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                File fileMetadata = new File();
                fileMetadata.setName(file.getOriginalFilename());
                fileMetadata.setParents(Collections.singletonList(MUSIC_FOLDER_ID));

                java.io.File tempFile = java.io.File.createTempFile("upload-", ".tmp");
                file.transferTo(tempFile);
                FileContent mediaContent = new FileContent(file.getContentType(), tempFile);

                File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id, name, mimeType, size, webContentLink")
                        .execute();

                log.info("File uploaded to Google Drive: {}", uploadedFile.getName());

                Map<String, String> fileResult = new HashMap<>();
                fileResult.put("fileName", file.getOriginalFilename());
                fileResult.put("fileId", uploadedFile.getId());
                fileResult.put("webContentLink", uploadedFile.getWebContentLink());
                results.add(fileResult);

                // Clean up temporary file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            } catch (Exception e) {
                log.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
                Map<String, String> errorResult = new HashMap<>();
                errorResult.put("fileName", file.getOriginalFilename());
                errorResult.put("error", e.getMessage());
                results.add(errorResult);
            }
        }

        return results;
    }

    public File uploadAlbumArtwork(java.io.File artworkFile, String albumName) throws IOException {
        if (artworkFile == null || !artworkFile.exists()) {
            log.warn("No artwork file provided for album: {}", albumName);
            return null;
        }

        // Generate a unique filename for the album cover
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String sanitizedAlbumName = albumName.replaceAll("[^a-zA-Z0-9\\s-]", "_").trim();
        String fileName = sanitizedAlbumName + "-cover-" + timestamp + ".jpg";

        // Create file metadata
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(ALBUM_COVER_FOLDER_ID));

        // Create FileContent
        FileContent mediaContent = new FileContent("image/jpeg", artworkFile);

        // Upload the file to Google Drive
        File uploadedFile = driveService
                .files()
                .create(fileMetadata, mediaContent)
                .setFields("id, name, mimeType, size, webContentLink")
                .execute();

        log.info("Uploaded album artwork for '{}' to Google Drive: {}", albumName, uploadedFile.getId());
        return uploadedFile;
    }

}