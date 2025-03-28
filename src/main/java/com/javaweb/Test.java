package com.javaweb;

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

    public static void main(String[] args) {
        // Specify the package/directory path
        String packagePath = "src/main/java/com/javaweb/view/imgs/avatars";

        try {
            Files.walk(Paths.get(packagePath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> System.out.println(FileUtil.getRelativeFilePath(path.toAbsolutePath().toFile())));
        } catch (IOException e) {
            System.err.println("Error traversing directory: " + e.getMessage());
        }
    }
}
