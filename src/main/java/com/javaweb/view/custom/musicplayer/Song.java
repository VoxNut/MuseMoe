package com.javaweb.view.custom.musicplayer;

import com.mpatric.mp3agic.Mp3File;
import lombok.Getter;
import net.coobird.thumbnailator.Thumbnails;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

@Getter
public class Song {
    private String songTitle;
    private String songArtist;
    private String songLength;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMilliseconds;
    private BufferedImage songImage;

    public Song(String filePath) {
        this.filePath = filePath;
        try {
            mp3File = new Mp3File(filePath);
            frameRatePerMilliseconds = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            songLength = convertToSongLengthFormat();

            // use the jaudiotagger library to create an audiofile obj to read mp3 file's information
            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            // read through the meta data of the audio file
            Tag tag = audioFile.getTag();
            if (tag != null) {
                songTitle = tag.getFirst(FieldKey.TITLE);
                songArtist = tag.getFirst(FieldKey.ARTIST);

                // Extract artwork
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    InputStream in = new ByteArrayInputStream(imageData);
                    BufferedImage originalImage = ImageIO.read(in);
                    songImage = Thumbnails.of(originalImage).size(300, 300).asBufferedImage();
                }
            } else {
                // could not read through mp3 file's meta data
                songTitle = "N/A";
                songArtist = "N/A";
                songLength = "00:00";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertToSongLengthFormat() {
        long minutes = mp3File.getLengthInSeconds() / 60;
        long seconds = mp3File.getLengthInSeconds() % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        return formattedTime;
    }

}