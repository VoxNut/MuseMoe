package com.javaweb.utils;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.impl.GoogleDriveService;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamingAudioPlayer {

    private final GoogleDriveService googleDriveService;
    private AudioDevice device;


    public AdvancedPlayer createPlayer(SongDTO songDTO, PlaybackListener listener) throws IOException, JavaLayerException {
        // Get the input stream from Google Drive
        InputStream inputStream = googleDriveService.getFileContent(songDTO.getDriveFileId());

        // Create buffered input stream for better performance
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        // Create the player
        device = FactoryRegistry.systemRegistry().createAudioDevice();
        AdvancedPlayer player = new AdvancedPlayer(bufferedInputStream, device);
        player.setPlayBackListener(listener);

        return player;
    }

    public AudioDevice getDevice() {
        return this.device;
    }


    public void extractMetadata(SongDTO songDTO) throws Exception {
        // Create a temporary file to analyze with JAudioTagger
        Path tempFile = Files.createTempFile("stream-", ".mp3");

        try (InputStream inputStream = googleDriveService.getFileContent(songDTO.getDriveFileId());
             OutputStream outputStream = new FileOutputStream(tempFile.toFile())) {

            // Download to temp file
            IOUtils.copy(inputStream, outputStream);

            // Extract metadata with JAudioTagger
            org.jaudiotagger.audio.AudioFile audioFile = AudioFileIO.read(tempFile.toFile());

            // Extract audio properties
            MP3AudioHeader audioHeader = (MP3AudioHeader) audioFile.getAudioHeader();

            //Bit rate
            songDTO.setBitrate(audioHeader.getBitRateAsNumber());

            // Set basic audio properties
            int duration = audioHeader.getTrackLength();
            songDTO.setDuration(duration);
            songDTO.setLengthInMilliseconds(duration * 1000);


            // Calculate frame rate (estimation)
            double frameRate = audioHeader.getNumberOfFrames() / (duration * 1000.0);
            songDTO.setFrameRatePerMilliseconds(frameRate);
            songDTO.setFrame(audioHeader.getNumberOfFrames());

            // Get ID3 tag information
            Tag tag = audioFile.getTag();
            if (tag != null) {
                songDTO.setTitle(tag.getFirst(FieldKey.TITLE));
                songDTO.setSongArtist(tag.getFirst(FieldKey.ARTIST));
                songDTO.setSongAlbum(tag.getFirst(FieldKey.ALBUM));
                songDTO.setGenre(tag.getFirst(FieldKey.GENRE));
                songDTO.setSongLyrics(tag.getFirst(FieldKey.LYRICS));
                if (!StringUtils.isBlank(tag.getFirst(FieldKey.YEAR))) {
                    songDTO.setReleaseYear(Integer.valueOf(tag.getFirst(FieldKey.YEAR)));
                }

                // Extract artwork
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
                    songDTO.setSongImage(image);
                }
            }

        } finally {
            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        }
    }

}