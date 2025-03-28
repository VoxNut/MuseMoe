package com.javaweb.utils;

import com.javaweb.model.dto.SongDTO;
import com.mpatric.mp3agic.Mp3File;
import net.coobird.thumbnailator.Thumbnails;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

@Component
public class Mp3Util {


    public void enrichSongDTO(SongDTO songDTO) {
        if (songDTO == null || songDTO.getAudioFilePath() == null) {
            return;
        }

        try {
            Mp3File mp3File = new Mp3File(songDTO.getAudioFilePath());
            songDTO.setMp3File(mp3File);
            songDTO.setFrameRatePerMilliseconds((double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds());
            songDTO.setSongLength(formatDuration(mp3File.getLengthInSeconds()));
            songDTO.setSongImage(setSongImageFromFileString(songDTO.getAudioFilePath()));
        } catch (Exception e) {
            songDTO.setSongLength("00:00");
        }
    }

    private String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    public BufferedImage setSongImageFromFileString(String audioFilePath) {
        if (audioFilePath == null) {
            return null;
        }
        try {
            AudioFile audioFile = AudioFileIO.read(new File(audioFilePath));
            Tag tag = audioFile.getTag();

            if (tag != null) {

                // Extract artwork
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    InputStream in = new ByteArrayInputStream(imageData);
                    BufferedImage originalImage = ImageIO.read(in);
                    originalImage = Thumbnails.of(originalImage).size(300, 300).asBufferedImage();
                    return originalImage;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}