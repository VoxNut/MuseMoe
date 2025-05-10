package test;

import com.javaweb.utils.FileUtil;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Test {
    private static void disableJaudiotaggerLogging() {
        // Get the global logger
        Logger rootLogger = Logger.getLogger("");

        // Set the global logging level to WARNING or higher
        rootLogger.setLevel(Level.WARNING);

        // Also set the level for all handlers to ensure it's properly applied
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.WARNING);
        }

        // Specifically target the Jaudiotagger logger
        Logger jaudiotaggerLogger = Logger.getLogger("org.jaudiotagger");
        jaudiotaggerLogger.setLevel(Level.SEVERE); // Only show severe errors
    }

    public static void main(String[] args) throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException, CannotWriteException {
        // Specify the package/directory path
        disableJaudiotaggerLogging();
//        String packagePath = "D:\\MuseMoe resources\\audio";
//        String packagePath = "D:\\MuseMoe resources\\imgs\\artist_profile";
//        String packagePath = "src/main/java/com/javaweb/view/mini_musicplayer/advertisement";
        String packagePath = "D:\\MuseMoe resources\\imgs\\avatars";
//        String packagePath = "D:\\MuseMoe resources\\imgs\\album_cover";
        File directory = new File(packagePath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Invalid directory path: " + packagePath);
            return;
        }

        try {
            Files.walk(Paths.get(packagePath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        File file = path.toFile();
                        String absolutePath = path.toFile().getAbsolutePath();
                        String relativePath = FileUtil.getRelativeFilePath(absolutePath);
                        System.out.println("\n===================================");
                        System.out.println("File path: " + relativePath);
                        System.out.println("File name: " + file.getName());

                        // Only process audio files
                        if (isAudioFile(file.getName())) {
                            try {
                                AudioFile audioFile = AudioFileIO.read(file);
                                MP3AudioHeader audioHeader = (MP3AudioHeader) audioFile.getAudioHeader();
                                Tag tag = audioFile.getTag();

                                if (tag != null) {
                                    String title = tag.getFirst(FieldKey.TITLE);
                                    String artist = tag.getFirst(FieldKey.ARTIST);
                                    String album = tag.getFirst(FieldKey.ALBUM);
                                    String quality = tag.getFirst(FieldKey.QUALITY);
                                    String year = tag.getFirst(FieldKey.YEAR);
                                    String originalYear = tag.getFirst(FieldKey.ORIGINAL_YEAR);
                                    String lyrics = tag.getFirst(FieldKey.LYRICS);
                                    String genre = tag.getFirst(FieldKey.GENRE);
                                    String rating = tag.getFirst(FieldKey.RATING);
                                    String language = tag.getFirst(FieldKey.LANGUAGE);
                                    // Print metadata with fallbacks for empty values
                                    System.out.println("Title: " + (title.isEmpty() ? "[No Title]" : title));
                                    System.out.println("Artist: " + (artist.isEmpty() ? "[Unknown Artist]" : artist));
                                    System.out.println("Album: " + (album.isEmpty() ? "[Unknown Album]" : album));
                                    System.out.println("Quality: " + (quality.isEmpty() ? "[Unknown Quality]" : quality));
                                    System.out.println("year: " + (year.isEmpty() ? "[Unknown year]" : year));
                                    System.out.println("Original Year: " + (originalYear.isEmpty() ? "[Unknown Original Year]" : originalYear));
                                    System.out.println("Lyrics: " + (lyrics.isEmpty() ? "[Unknown lyrics]" : lyrics));
                                    System.out.println("Genre: " + (genre.isEmpty() ? "[Unknown Genre]" : genre));
                                    System.out.println("Rating: " + (rating.isEmpty() ? "[Unknown Rating]" : rating));
                                    System.out.println("Language: " + (language.isEmpty() ? "[Unknown Language]" : language));
                                    // Check for artwork
                                    Artwork artwork = tag.getFirstArtwork();
                                    System.out.println("Has Artwork: " + (artwork != null));
                                } else {
                                    System.out.println("No metadata tags found in file");
                                }

                                // Print audio format information
                                System.out.println("Sample Rate: " + audioFile.getAudioHeader().getSampleRate() + " Hz");
                                System.out.println("Bit Rate: " + audioFile.getAudioHeader().getBitRate() + " kbps");
//                                System.out.println("Channels: " + audioFile.getAudioHeader().getChannels());
                                System.out.println("Length: " + audioFile.getAudioHeader().getTrackLength());
                                System.out.println("Number of frames: " + audioHeader.getNumberOfFrames());
                                System.out.println("Number of bit rates: " + audioHeader.getBitRateAsNumber());

                            } catch (Exception e) {
                                System.err.println("Error processing audio file: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Not an audio file (skipping metadata extraction)");
                        }
                        System.out.println("File size: " + FileUtil.getFileSize(relativePath));
                        System.out.println("===================================");
                    });
        } catch (IOException e) {
            System.err.println("Error traversing directory: " + e.getMessage());
            e.printStackTrace();
        }


    }

    /**
     * Checks if a file is an audio file based on its extension
     */
    private static boolean isAudioFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals("mp3") || extension.equals("wav") ||
                extension.equals("flac") || extension.equals("m4a") ||
                extension.equals("aac") || extension.equals("ogg");
    }

    /**
     * Gets the file extension from a filename
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

}
