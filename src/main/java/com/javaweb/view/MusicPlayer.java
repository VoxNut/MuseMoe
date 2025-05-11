package com.javaweb.view;

import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.ImageMediaUtil;
import com.javaweb.utils.StreamingAudioPlayer;
import com.javaweb.view.advertisement.AdvertisementManager;
import com.javaweb.view.event.MusicPlayerMediator;
import com.javaweb.view.state.PlayerState;
import com.javaweb.view.state.StoppedState;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor

public class MusicPlayer extends PlaybackListener {

    private final StreamingAudioPlayer streamingPlayer;
    private final ImageMediaUtil imageMediaUtil;
    private final MusicPlayerMediator mediator;
    private final AdvertisementManager adManager;
    private static final Object playSignal = new Object();


    @Getter
    @Setter
    private PlayerState currentState = new StoppedState(this);

    @Getter
    @Setter
    private SongDTO currentSong;

    @Getter
    @Setter
    private PlaylistDTO currentPlaylist;

    private int currentPlaylistIndex;

    // use JLayer library to create an AdvancedPlayer obj which will handle playing
    // the music
    private AdvancedPlayer advancedPlayer;

    // pause boolean flag used to indicate whether the player has been paused
    @Getter
    private boolean isPaused;

    // boolean flag used to tell when the song has finished
    private boolean songFinished;

    private boolean pressedNext, pressedPrev;

    private boolean pressedShuffle;

    private boolean pressedReplay;


    private FloatControl volumeControl;
    private AudioDevice device;

    // stores in the last frame when the playback is finished (used for pausing and
    // resuming)
    @Getter
    @Setter
    private int currentFrame;

    // track how many milliseconds has passed since playing the song (used for
    // updating the slider)
    @Setter
    @Getter
    private int currentTimeInMilli;

    private Thread sliderThread;


    private BufferedInputStream bufferedInputStream;
    private FileInputStream fileInputStream;


    @Getter
    long calculatedFrame;

    @Getter
    private boolean havingAd;

    @Getter
    private RepeatMode repeatMode = RepeatMode.NO_REPEAT;

    @Getter
    private float currentVolumeGain = 0.0f;


    public void loadLocalSong(SongDTO song) throws IOException {
        if (song == null || !song.getIsLocalFile()) {
            log.error("Attempted to load non-local song with local method");
            return;
        }

        // Reset state
        resetPlaybackPosition();
        stopSong();

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Set current song and update UI
        currentSong = song;
        havingAd = false;
        mediator.notifyAdOff();

        if (repeatMode == RepeatMode.REPEAT_ONE) {
            repeatMode = RepeatMode.REPEAT_ALL;
        }

        mediator.notifyRepeatModeChanged(repeatMode);
        mediator.notifyHomePagePlaybackSlider();
        updateGUI();

        playCurrentSong();
    }

    // Add import for FactoryRegistry at the top of the file
    public void loadSong(SongDTO song) throws IOException {
        if (adManager.shouldShowAd(getCurrentUser()) || song != null) {
            processLoadSong(song);
            return;
        }

        // Show loading indicator
        mediator.notifyLoadingStarted();

        // Load song in background
        CompletableFuture.supplyAsync(() -> {
            try {
                return CommonApiUtil.fetchSongById(song.getId());
            } catch (Exception e) {
                e.printStackTrace();
                return song;
            }
        }).thenAccept(loadedSong -> {
            // Back on UI thread
            SwingUtilities.invokeLater(() -> {
                try {
                    mediator.notifyLoadingFinished();
                    processLoadSong(loadedSong);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void processLoadSong(SongDTO song) throws IOException {
        resetPlaybackPosition();
        stopSong();

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Rest of your existing loadSong logic
        if (adManager.shouldShowAd(getCurrentUser())) {
            if (song != null) {
                adManager.storeLastSong(song);
            }
            havingAd = true;
            //get random ad url
            String driveId = adManager.getAdvertisements()[(int) (Math.random() * adManager.getAdvertisements().length)];
            currentSong = CommonApiUtil.fetchSongByGoogleDriveId(driveId);
            mediator.notifyAdOn();
        } else {
            havingAd = false;
            currentSong = song;
            mediator.notifyAdOff();

            if (currentSong != null && !havingAd) {
                CommonApiUtil.logPlayHistory(currentSong.getId());
            }
        }

        // play the current song if not null
        if (currentSong != null) {
            // set the currentPlaylistIndex only if the currentSong is not null.
            if (currentPlaylist != null) {
                currentPlaylistIndex = currentPlaylist.getIndexFromSong(currentSong);
            }

            if (repeatMode == RepeatMode.REPEAT_ONE) {
                repeatMode = RepeatMode.REPEAT_ALL;
            }
            mediator.notifyRepeatModeChanged(repeatMode);
            mediator.notifyHomePagePlaybackSlider();
            updateGUI();
            playCurrentSong();
        }
    }


    public void pauseSong() throws IOException {
        if (havingAd) return;
        if (advancedPlayer != null) {
            isPaused = true;
            stopSong();
        } else {
            System.out.println("Cannot pause because advancedPlayer is null.");
        }
    }


    public void stopSong() throws IOException {
        if (sliderThread != null) {
            sliderThread.interrupt();
            try {
                sliderThread.join(100);
            } catch (InterruptedException ignored) {

            }
            sliderThread = null;
        }

        if (advancedPlayer != null) {
            try {
                if (!songFinished) {
                    advancedPlayer.stop();
                }
            } catch (Exception e) {
                System.out.println("Error closing player: " + e.getMessage());
            } finally {
                advancedPlayer = null;
            }
        } else {
            adManager.getUserPlayCounter().put(getCurrentUser().getId(), 0);
        }

        if (fileInputStream != null) {
            fileInputStream.close();
            fileInputStream = null;
        }

        if (bufferedInputStream != null) {
            bufferedInputStream.close();
            bufferedInputStream = null;
        }

        mediator.notifyToggleCava(false);

    }

    public void nextSong() throws IOException {
        //Stop any further features when having an ad.
        if (havingAd) return;
        pressedNext = true;

        if (currentPlaylist == null) {
            if (repeatMode == RepeatMode.REPEAT_ONE) {
                repeatMode = RepeatMode.REPEAT_ALL;
            }
        } else {
            // If the playing song is the last song in playlist.
            if (currentPlaylistIndex + 1 == currentPlaylist.size()) {
                if (repeatMode == RepeatMode.NO_REPEAT) {
                    // Fetch user playlists and play random playlist
                    List<PlaylistDTO> playlists = CommonApiUtil.fetchPlaylistByUserId()
                            .stream()
                            .filter(playlist -> !playlist.isEmptyList())
                            .collect(Collectors.toList());
                    // Only check if the user has more than 1 playlist or else play the first song in current playlist.
                    if (playlists.size() != 1) {
                        playlists = playlists.stream()
                                .filter(playlistDTO -> !playlistDTO.equals(currentPlaylist))
                                .toList();
                    }
                    currentPlaylist = playlists.get((int) (Math.random() * playlists.size()));
                    currentPlaylistIndex = 0;
                    currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                } else if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                    currentPlaylistIndex = 0;
                    currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                    if (repeatMode == RepeatMode.REPEAT_ONE) {
                        repeatMode = RepeatMode.REPEAT_ALL;
                    }
                }
            } else {
                currentPlaylistIndex++;
                currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                }
            }
        }
        loadSong(currentSong);
    }

    public void prevSong() throws IOException {
        //Stop any further features when having an ad.
        if (havingAd) return;
        // A single song
        pressedPrev = true;

        if (currentPlaylist == null) {
            if (repeatMode == RepeatMode.REPEAT_ONE) {
                repeatMode = RepeatMode.REPEAT_ALL;
            }
        } else {
            // Have a playlist
            // check to see if we have reached the head of the playlist
            if (currentPlaylistIndex == 0) {
                if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                    currentPlaylistIndex = currentPlaylist.size() - 1;
                    currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                    if (repeatMode == RepeatMode.REPEAT_ONE) {
                        repeatMode = RepeatMode.REPEAT_ALL;
                    }
                }
            } else {
                // increase current playlist index
                currentPlaylistIndex--;
                // update current song
                currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                }
            }
        }
        loadSong(currentSong);
    }

    public void playCurrentSong() {
        try {
            volumeControl = null;

            // Check if this is a local file
            if (currentSong != null && currentSong.getIsLocalFile() && currentSong.getLocalFilePath() != null) {
                playLocalSongInternal();
            } else {
                // Stream from Google Drive
                playStreamingSongInternal();
            }

        } catch (Exception e) {
            log.error("Error playing song: {}", e.getMessage(), e);
        }
    }


    private void playLocalSongInternal() {
        try {
            fileInputStream = new FileInputStream(currentSong.getLocalFilePath());
            bufferedInputStream = new BufferedInputStream(fileInputStream);

            device = FactoryRegistry.systemRegistry().createAudioDevice();
            advancedPlayer = new AdvancedPlayer(bufferedInputStream, device);
            advancedPlayer.setPlayBackListener(this);

            setVolume(currentVolumeGain);

            SwingUtilities.invokeLater(() -> {
                mediator.notifyToggleCava(true);
                startMusicThread();
                startPlaybackSliderThread();
            });
        } catch (Exception e) {
            log.error("Error playing local song: {}", e.getMessage(), e);
        }
    }

    private void playStreamingSongInternal() {
        try {
            advancedPlayer = streamingPlayer.createPlayer(currentSong, this);
            device = streamingPlayer.getDevice();

            setVolume(currentVolumeGain);

            SwingUtilities.invokeLater(() -> {
                mediator.notifyToggleCava(true);
                startMusicThread();
                startPlaybackSliderThread();
            });
        } catch (Exception e) {
            log.error("Error playing streaming song: {}", e.getMessage(), e);
        }
    }

    // create a thread that will handle playing the music
    private void startMusicThread() {
        new Thread(() -> {
            try {
                if (isPaused || pressedReplay) {
                    synchronized (playSignal) {
                        isPaused = false;
                        pressedReplay = false;
                        playSignal.notify();
                    }
                    // Play from the current frame
                    System.out.println(currentFrame);
                    advancedPlayer.play(currentFrame, Integer.MAX_VALUE);

                } else {
                    // Play from the beginning
                    advancedPlayer.play();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    // create a thread that will handle updating the slider
    private void startPlaybackSliderThread() {
        if (sliderThread != null && sliderThread.isAlive()) {
            sliderThread.interrupt();
            try {
                sliderThread.join(200);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        sliderThread = new Thread(() -> {
            try {
                SongDTO currentSongCopy = currentSong;
                if (currentSongCopy == null) return;

                long totalFrames = currentSongCopy.getFrame();
                double frameRate = currentSongCopy.getFrameRatePerMilliseconds();
                long songDurationMs = currentSongCopy.getLengthInMilliseconds();

                if (isPaused) {
                    synchronized (playSignal) {
                        try {
                            playSignal.wait(5000);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }

                // Adjust startTime to account for the currentTimeInMilli
                long startTime = System.currentTimeMillis() - currentTimeInMilli;

                // Main update loop
                while (!Thread.currentThread().isInterrupted()) {
                    // Check if we should exit the loop
                    if (isPaused || songFinished || pressedNext || pressedPrev ||
                            pressedShuffle || pressedReplay || advancedPlayer == null) {
                        break;
                    }

                    // Calculate elapsed time
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - startTime;
                    currentTimeInMilli = (int) elapsedTime;

                    // Calculate frame position
                    calculatedFrame = (int) (elapsedTime * frameRate);

                    // Ensure we don't exceed song boundaries
                    if (calculatedFrame > totalFrames) {
                        calculatedFrame = totalFrames;
                    }

                    if (!Thread.currentThread().isInterrupted() && !isPaused &&
                            !songFinished && advancedPlayer != null) {
                        mediator.notifyPlaybackProgress((int) calculatedFrame, currentTimeInMilli);
                    }

                    // Exit if we've reached the end
                    if (calculatedFrame >= totalFrames || elapsedTime >= songDurationMs) {
                        break;
                    }

                    // Sleep for a very short time to reduce CPU usage
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Playback slider thread error: " + e.getMessage());
                e.printStackTrace();
            }
        }, "PlaybackSliderThread");

        sliderThread.setDaemon(true);
        sliderThread.start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {

        // this method gets called in the beginning of the song
        System.out.println("Playback Started");

        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
        pressedShuffle = false;
        pressedReplay = false;

        mediator.notifyPlaybackStarted(currentSong);

        // Reapply current volume
        Timer volumeTimer = new Timer(1, e -> {
            setVolume(currentVolumeGain);
        });
        volumeTimer.setRepeats(false);
        volumeTimer.start();

    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        // This method gets called when the song finishes or if the player gets closed
        System.out.println("Playback Finished!");
        // Play the previous last song when the ad finished
        if (havingAd) {
            havingAd = false;
            songFinished = true;
            // This user is initialized through login. So no need to fetch from db again.
            adManager.resetUserCounter(getCurrentUser().getId());
            currentSong = adManager.getLastSongDTO();
            try {
                loadSong(currentSong);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (isPaused) {
                // If paused, update the current frame for resuming later
                currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
                System.out.println(currentFrame);
                return;
            }
            if (pressedReplay) return;


            if (pressedNext || pressedPrev || pressedShuffle) {
                adManager.updateUserPlayCounter(getCurrentUser());
                return;
            }

            long totalFrames = currentSong.getFrame();
            int threshold = (int) (totalFrames * 0.95);

            //Naturally end
            if (calculatedFrame >= threshold) {
                mediator.notifyPlaybackPaused(currentSong);
                //Update play counter
                adManager.updateUserPlayCounter(getCurrentUser());
                if (currentPlaylist == null) {
                    // Single song mode
                    try {
                        handleSingleSongCompletion();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        handlePlaylistSongCompletion();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void handleSingleSongCompletion() throws IOException {
        if (repeatMode == RepeatMode.REPEAT_ONE || repeatMode == RepeatMode.REPEAT_ALL) {
            resetPlaybackPosition();
            playCurrentSong();
        } else {
            mediator.notifyPlaybackPaused(currentSong);
            songFinished = true;
        }
    }

    private void handlePlaylistSongCompletion() throws IOException {
        if (currentPlaylistIndex == currentPlaylist.size() - 1) {
            if (repeatMode == RepeatMode.REPEAT_ALL) {
                currentPlaylistIndex = 0;
                currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                resetPlaybackPosition();
                playCurrentSong();

            } else if (repeatMode == RepeatMode.REPEAT_ONE) {
                resetPlaybackPosition();
                playCurrentSong();

            } else {
                mediator.notifyPlaybackPaused(currentSong);
                songFinished = true;
            }
        } else {
            if (repeatMode == RepeatMode.REPEAT_ONE) {
                loadSong(currentSong);
            } else {
                try {
                    nextSong();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetPlaybackPosition() {
        currentTimeInMilli = 0;
        currentFrame = 0;
    }

    public void replayFiveSeconds() throws IOException {
        if (havingAd) return;
        if (currentSong == null) return;

        pressedReplay = true;

        long newPosition = currentTimeInMilli - 5000;
        if (newPosition < 0) {
            newPosition = 0;
        }

        stopSong();

        // Update current position
        currentTimeInMilli = (int) newPosition;
        currentFrame = (int) (newPosition * currentSong.getFrameRatePerMilliseconds());


        playCurrentSong();
    }

    public void shufflePlaylist() throws IOException {

        if (currentPlaylist == null || currentPlaylist.size() <= 1) {
            return;
        }

        pressedShuffle = true;
        currentPlaylistIndex = currentPlaylist.getRandomSongIndex();
        currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);


        loadSong(currentSong);

    }

    public void setVolume(float gain) {
        this.currentVolumeGain = Math.min(Math.max(gain, -40f), 40f);

        if (this.device == null) {
            return;
        }

        try {
            if (this.volumeControl == null) {
                Class<JavaSoundAudioDevice> clazz = JavaSoundAudioDevice.class;
                Field[] fields = clazz.getDeclaredFields();

                SourceDataLine source = null;
                for (Field field : fields) {
                    if ("source".equals(field.getName())) {
                        field.setAccessible(true);
                        Object sourceObj = field.get(this.device);
                        field.setAccessible(false);

                        if (sourceObj != null) {
                            source = (SourceDataLine) sourceObj;
                        }
                    }
                }

                if (source != null && source.isOpen()) {
                    try {
                        this.volumeControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
                    } catch (IllegalArgumentException e) {
                        System.out.println("This audio device doesn't support volume control: " + e.getMessage());
                        return;
                    }
                } else {
                    return;
                }
            }

            if (this.volumeControl != null) {
                float newGain = Math.min(Math.max(currentVolumeGain, volumeControl.getMinimum()), volumeControl.getMaximum());
                volumeControl.setValue(newGain);
            }
        } catch (Exception e) {
            System.err.println("Error setting volume: " + e.getMessage());
        }
    }

    public void cycleRepeatMode() throws IOException {
        switch (repeatMode) {
            case NO_REPEAT:
                repeatMode = RepeatMode.REPEAT_ALL;
                break;
            case REPEAT_ALL:
                repeatMode = RepeatMode.REPEAT_ONE;
                break;
            case REPEAT_ONE:
                repeatMode = RepeatMode.NO_REPEAT;
                break;
        }
    }

    private void updateGUI() {
        imageMediaUtil.loadAndWaitForImage(currentSong, 700);
        updateThemeFromSong(currentSong);
        mediator.notifySongLoaded(currentSong);
        if (currentPlaylist != null) {
            mediator.notifyPlaylistLoaded(currentPlaylist);
        } else if (currentSong.getSongAlbum() != null) {
            mediator.notifySongAlbum(currentSong.getSongAlbum());
        }
    }

    private UserDTO getCurrentUser() {
        return UserSessionManager.getInstance().getCurrentUser();
    }


    private void updateThemeFromSong(SongDTO song) {
        if (song != null && song.getSongImage() != null) {
            try {
                MiniMusicPlayerGUI.getInstance();
                Color[] themeColors = GuiUtil.extractThemeColors(song.getSongImage());
                ThemeManager.getInstance().setThemeColors(
                        themeColors[0],
                        themeColors[1],
                        themeColors[2]
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}