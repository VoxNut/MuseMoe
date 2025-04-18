package test;

import com.javaweb.utils.FileUtil;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Test {
    public static boolean hasArtwork(String filePath) {
        try {
            // Use the jaudiotagger library to create an audiofile object to read mp3 file's
            // information
            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            // Read through the metadata of the audio file
            Tag tag = audioFile.getTag();
            if (tag != null) {
                // Extract artwork
                Artwork artwork = tag.getFirstArtwork();
                System.out.println(tag.getFirst(FieldKey.TITLE));
                System.out.println(tag.getFirst(FieldKey.ARTIST));
                return artwork != null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static double getAverageVolume(String filePath) throws Exception {
        Bitstream bitstream = new Bitstream(new FileInputStream(filePath));
        Header frameHeader;
        SampleBuffer sampleBuffer;
        long totalSamples = 0;
        double totalAmplitude = 0;

        while ((frameHeader = bitstream.readFrame()) != null) {
            Decoder decoder = new Decoder();
            sampleBuffer = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);

            short[] samples = sampleBuffer.getBuffer();
            for (short sample : samples) {
                totalAmplitude += Math.abs(sample);
                totalSamples++;
            }

            bitstream.closeFrame();
        }
        bitstream.close();
        return totalAmplitude / totalSamples;
    }

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

    public static void main(String[] args) {
        // Specify the package/directory path
        disableJaudiotaggerLogging();
        String packagePath = "src/main/java/com/javaweb/view/mini_musicplayer/audio";
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
                                Tag tag = audioFile.getTag();

                                if (tag != null) {
                                    String title = tag.getFirst(FieldKey.TITLE);
                                    String artist = tag.getFirst(FieldKey.ARTIST);
                                    String album = tag.getFirst(FieldKey.ALBUM);

                                    // Print metadata with fallbacks for empty values
                                    System.out.println("Title: " + (title.isEmpty() ? "[No Title]" : title));
                                    System.out.println("Artist: " + (artist.isEmpty() ? "[Unknown Artist]" : artist));
                                    System.out.println("Album: " + (album.isEmpty() ? "[Unknown Album]" : album));

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

    /**
     * Formats duration in seconds to mm:ss format
     */
    private static String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}
