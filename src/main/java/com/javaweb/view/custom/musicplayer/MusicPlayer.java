package com.javaweb.view.custom.musicplayer;

import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.CommonApiUtil;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;


public class MusicPlayer extends PlaybackListener {
    // this will be used to update isPaused more synchronously
    private static final Object playSignal = new Object();

    // need reference so that we can update the gui in this class
    private final MusicPlayerGUI musicPlayerGUI;

    // we will need a way to store our song's details, so we will be creating a song
    // class
    @Getter
    @Setter
    private SongDTO currentSong;

    @Setter
    private PlaylistDTO currentPlaylist;

    // we will need to keep track the index we are in the playlist
    private int currentPlaylistIndex;

    // use JLayer library to create an AdvancedPlayer obj which will handle playing
    // the music
    private AdvancedPlayer advancedPlayer;

    // pause boolean flag used to indicate whether the player has been paused
    private boolean isPaused;

    // boolean flag used to tell when the song has finished
    private boolean songFinished;

    private boolean pressedNext, pressedPrev;

    private boolean pressedShuffle;

    private boolean pressedReplay;

    @Getter
    private RepeatMode repeatMode = RepeatMode.NO_REPEAT;

    private FloatControl volumeControl;
    private AudioDevice device;

    // stores in the last frame when the playback is finished (used for pausing and
    // resuming)
    @Setter
    private int currentFrame;

    // track how many milliseconds has passed since playing the song (used for
    // updating the slider)
    @Setter
    private int currentTimeInMilli;

    private Thread sliderThread;


    private BufferedInputStream bufferedInputStream;
    private FileInputStream fileInputStream;

    private final AdvertisementManager adManager;


    @Getter
    int calculatedFrame;

    @Getter
    private boolean hasAd;

    @Setter
    private UserDTO currentUser;


    // constructor
    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        this.adManager = AdvertisementManager.getInstance();
    }

    public void loadSong(SongDTO song) throws IOException {

        // stop the song if possible
        if (!songFinished)
            stopSong();

        if (currentPlaylist != null) {
            currentPlaylistIndex = currentPlaylist.getIndexFromSong(currentSong);
        }


        //Check if Free user has meet the ads condition.
        if (adManager.shouldShowAd(currentUser)) {
            // Store the original song URL before playing the ad
            if (song != null) {
                adManager.storeLastSong(song);
            }
            hasAd = true;
            //get random ad url
            String randomAdUrl = adManager.getAdvertisements()[(int) (Math.random() * adManager.getAdvertisements().length)];
            currentSong = CommonApiUtil.fetchSongByUrl(randomAdUrl);
            musicPlayerGUI.getHeartButton().setVisible(false);
            musicPlayerGUI.getShuffleButton().setVisible(false);
            musicPlayerGUI.getRepeatButton().setVisible(false);
            musicPlayerGUI.getReplayButton().setVisible(false);
        } else {
            musicPlayerGUI.getHeartButton().setVisible(true);
            musicPlayerGUI.getShuffleButton().setVisible(true);
            musicPlayerGUI.getRepeatButton().setVisible(true);
            musicPlayerGUI.getReplayButton().setVisible(true);
            hasAd = false;
            currentSong = song;
        }

        // play the current song if not null
        if (currentSong != null) {
            // update gui
            updateUI();
            if (repeatMode == RepeatMode.REPEAT_ONE) {
                repeatMode = RepeatMode.REPEAT_ALL;
                musicPlayerGUI.updateRepeatButtonIcon();
            }
            musicPlayerGUI.getHomePage().showMusicPlayerHeader();
            playCurrentSong();

        }
    }


    public void pauseSong() throws IOException {
        if (hasAd) return;
        if (advancedPlayer != null) {
            isPaused = true;
            musicPlayerGUI.enablePlayButtonDisablePauseButton();
            musicPlayerGUI.getHomePage().enablePlayButtonDisablePauseButton();
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
            adManager.getUserPlayCounter().put(currentUser.getId(), 0);
        }

        if (fileInputStream != null) {
            fileInputStream.close();
            fileInputStream = null;
        }

        if (bufferedInputStream != null) {
            bufferedInputStream.close();
            bufferedInputStream = null;
        }
    }

    public void nextSong() throws IOException {
        if (hasAd) return;
        pressedNext = true;
        if (currentPlaylist == null) {
            if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                updateUI();
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                    musicPlayerGUI.updateRepeatButtonIcon();
                }
                loadSong(currentSong);
            }
        } else {
            if (currentPlaylistIndex + 1 == currentPlaylist.size()) {
                if (repeatMode == RepeatMode.NO_REPEAT) {
                    // Fetch user playlists and play random playlist
                    List<PlaylistDTO> playlists = CommonApiUtil.fetchPlaylistByUserId()
                            .stream()
                            .filter(playlist -> !playlist.isEmptyList())
                            .collect(Collectors.toList());
                    if (playlists.size() != 1) {
                        playlists = playlists.stream()
                                .filter(playlistDTO -> !playlistDTO.equals(currentPlaylist))
                                .collect(Collectors.toList());
                    }
                    currentPlaylist = playlists.get((int) (Math.random() * playlists.size()));
                    currentPlaylistIndex = 0;
                    currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                    loadSong(currentSong);
                } else if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                    currentPlaylistIndex = 0;
                    currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                    updateUI();
                    if (repeatMode == RepeatMode.REPEAT_ONE) {
                        repeatMode = RepeatMode.REPEAT_ALL;
                        musicPlayerGUI.updateRepeatButtonIcon();
                    }
                    loadSong(currentSong);
                }
            } else {
                currentPlaylistIndex++;
                currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                updateUI();
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                    musicPlayerGUI.updateRepeatButtonIcon();
                }
                loadSong(currentSong);

            }

        }

    }

    public void prevSong() throws IOException {
        if (hasAd) return;
        // A single song
        if (currentPlaylist == null) {
            if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                pressedPrev = true;
                updateUI();
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                    musicPlayerGUI.updateRepeatButtonIcon();
                }
                loadSong(currentSong);
            }
        } else {
            // Have a playlist
            pressedPrev = true;

            // check to see if we have reached the head of the playlist
            if (currentPlaylistIndex == 0) {
                if (repeatMode == RepeatMode.NO_REPEAT) {
                    updateUI();
                    loadSong(currentSong);
                } else if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                    currentPlaylistIndex = currentPlaylist.size() - 1;
                    currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                    updateUI();
                    if (repeatMode == RepeatMode.REPEAT_ONE) {
                        repeatMode = RepeatMode.REPEAT_ALL;
                        musicPlayerGUI.updateRepeatButtonIcon();
                    }
                    loadSong(currentSong);
                }
                // Random song
            } else {
                // increase current playlist index
                currentPlaylistIndex--;
                // update current song
                currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                updateUI();
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                    musicPlayerGUI.updateRepeatButtonIcon();
                }
                // play the song
                loadSong(currentSong);
            }

        }
    }

    public void playCurrentSong() {
        try {
            fileInputStream = new FileInputStream(currentSong.getAudioFilePath());
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            device = FactoryRegistry.systemRegistry().createAudioDevice();
            advancedPlayer = new AdvancedPlayer(bufferedInputStream, device);

            advancedPlayer.setPlayBackListener(this);
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.getHomePage().enablePauseButtonDisablePlayButton();
            startMusicThread();
            startPlaybackSliderThread();

            if (!hasAd) {
                CommonApiUtil.logPlayHistory(currentSong.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            sliderThread.interrupt(); // Stop existing thread if any
        }

        sliderThread = new Thread(() -> {
            try {
                if (isPaused) {
                    synchronized (playSignal) {
                        while (isPaused) { // Use while loop for safety
                            playSignal.wait(); // Add timeout
                        }
                    }
                }

                // Adjust startTime to account for the currentTimeInMilli
                long startTime = System.currentTimeMillis() - currentTimeInMilli;
                int totalFrames = currentSong.getMp3File().getFrameCount();
                double frameRate = currentSong.getFrameRatePerMilliseconds();
                long songDurationMs = currentSong.getMp3File().getLengthInMilliseconds();

                while (!Thread.currentThread().isInterrupted()) {
                    if (isPaused || songFinished || pressedNext || pressedPrev || pressedShuffle || pressedReplay) {
                        break;
                    }

                    // Calculate elapsed time
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - startTime;
                    currentTimeInMilli = (int) elapsedTime;

                    // Calculate frame position
                    calculatedFrame = (int) (elapsedTime * frameRate);

                    // Update UI on EDT
                    if (calculatedFrame <= totalFrames && elapsedTime <= songDurationMs) {
                        musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);
                        musicPlayerGUI.getHomePage().setPlaybackSliderValue(calculatedFrame);
                        musicPlayerGUI.updateSongTimeLabel(currentTimeInMilli);
                        musicPlayerGUI.getHomePage().updateSongTimeLabel(currentTimeInMilli);
                    } else {
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });

        sliderThread.setDaemon(true);
        sliderThread.start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        // this method gets called in the beginning of the song
        System.out.println("Playback Started");
        volumeControl = null;
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
        pressedShuffle = false;
        pressedReplay = false;

    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        // This method gets called when the song finishes or if the player gets closed
        System.out.println("Playback Finished!");
        // Play the previous last song
        if (hasAd) {
            hasAd = false;
            songFinished = true;
            UserDTO userDTO = musicPlayerGUI.getHomePage().getCurrentUser();
            adManager.resetUserCounter(userDTO.getId());
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
                adManager.updateUserPlayCounter(currentUser);
                return;
            }

            int totalFrames = currentSong.getMp3File().getFrameCount();
            int threshold = (int) (totalFrames * 0.95);

            //Naturally end
            if (calculatedFrame >= threshold) {
                //Update play counter
                adManager.updateUserPlayCounter(currentUser);
                if (currentPlaylist == null) {
                    // Single song mode
                    try {
                        handleSingleSongCompletion();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Playlist mode
                    try {
                        handlePlaylistSongCompletion();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    // Helper method for single song completion logic
    private void handleSingleSongCompletion() throws IOException {
        if (repeatMode == RepeatMode.REPEAT_ONE || repeatMode == RepeatMode.REPEAT_ALL) {
            resetPlaybackPosition();
            loadSong(currentSong);
        } else {
            songFinished = true;
            musicPlayerGUI.enablePlayButtonDisablePauseButton();
            musicPlayerGUI.getHomePage().enablePlayButtonDisablePauseButton();
        }
    }

    // Helper method for playlist song completion logic
    private void handlePlaylistSongCompletion() throws IOException {
        // Last song in the playlist
        if (currentPlaylistIndex == currentPlaylist.size() - 1) {
            if (repeatMode == RepeatMode.REPEAT_ALL) {
                // Loop back to the beginning of the playlist
                currentPlaylistIndex = 0;
                currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);
                try {
                    updateUI();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                loadSong(currentSong);
            } else if (repeatMode == RepeatMode.REPEAT_ONE) {
                // Repeat the current song
                resetPlaybackPosition();
                loadSong(currentSong);
            } else {
                // End of playlist with no repeat
                songFinished = true;
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
                musicPlayerGUI.getHomePage().enablePlayButtonDisablePauseButton();
            }
        } else {
            if (repeatMode == RepeatMode.REPEAT_ONE) {
                // Repeat the current song
                resetPlaybackPosition();
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

    // Helper method to reset playback position
    private void resetPlaybackPosition() {
        currentTimeInMilli = 0;
        currentFrame = 0;
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        musicPlayerGUI.getHomePage().updatePlaybackSlider(currentSong);

        musicPlayerGUI.setPlaybackSliderValue(0);
        musicPlayerGUI.getHomePage().setPlaybackSliderValue(0);
        musicPlayerGUI.setVolumeSliderValue(0);
    }

    public void replayFiveSeconds() throws IOException {
        if (hasAd) return;
        if (currentSong == null) return;

        // Set replay flag
        pressedReplay = true;

        // Calculate new position
        long newPosition = currentTimeInMilli - 5000;
        if (newPosition < 0) {
            newPosition = 0;
        }

        // Stop current playback
        stopSong();

        // Update current position
        currentTimeInMilli = (int) newPosition;
        currentFrame = (int) (newPosition * currentSong.getFrameRatePerMilliseconds());

        // Update UI sliders
        musicPlayerGUI.setPlaybackSliderValue(currentFrame);
        musicPlayerGUI.getHomePage().setPlaybackSliderValue(currentFrame);

        // Start playback from new position
        playCurrentSong();
    }

    public void shufflePlaylist() throws IOException {

        if (currentPlaylist == null || currentPlaylist.size() <= 1) {
            return;
        }

        pressedShuffle = true;
        currentPlaylistIndex = currentPlaylist.getRandomSongIndex();
        currentSong = currentPlaylist.getSongAt(currentPlaylistIndex);

        currentFrame = 0;
        currentTimeInMilli = 0;

        musicPlayerGUI.setPlaybackSliderValue(0);
        musicPlayerGUI.getHomePage().setPlaybackSliderValue(0);
        musicPlayerGUI.setVolumeSliderValue(0);

        musicPlayerGUI.getHomePage().updateSpinningDisc(currentSong);
        musicPlayerGUI.getHomePage().updateScrollingText(currentSong);

        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updateSongImage(currentSong);

        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.getHomePage().enablePauseButtonDisablePlayButton();
        loadSong(currentSong);

    }

    public void setVolume(float gain) {
        if (this.volumeControl == null) {
            Class<JavaSoundAudioDevice> clazz = JavaSoundAudioDevice.class;
            Field[] fields = clazz.getDeclaredFields();
            try {
                SourceDataLine source = null;
                for (Field field : fields) {
                    if ("source".equals(field.getName())) {
                        field.setAccessible(true);
                        source = (SourceDataLine) field.get(this.device);
                        field.setAccessible(false);
                        this.volumeControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.volumeControl != null) {
            float newGain = Math.min(Math.max(gain, volumeControl.getMinimum()), volumeControl.getMaximum());
            System.out.println("Was: " + volumeControl.getValue() + " Will be: " + newGain);

            volumeControl.setValue(newGain);
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
        musicPlayerGUI.updateRepeatButtonIcon();
    }

    private void updateUI() throws IOException {
        currentTimeInMilli = 0;
        currentFrame = 0;

        musicPlayerGUI.updatePlaybackSlider(currentSong);
        musicPlayerGUI.getHomePage().updatePlaybackSlider(currentSong);

        musicPlayerGUI.setPlaybackSliderValue(0);
        musicPlayerGUI.getHomePage().setPlaybackSliderValue(0);
        musicPlayerGUI.setVolumeSliderValue(0);

        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.getHomePage().enablePauseButtonDisablePlayButton();

        musicPlayerGUI.updateSongDetails(currentSong);

        musicPlayerGUI.getHomePage().updateSpinningDisc(currentSong);
        musicPlayerGUI.getHomePage().updateScrollingText(currentSong);

        if (currentPlaylist != null) {
            musicPlayerGUI.getPlaylistNameLabel().setText(currentPlaylist.getName());
            musicPlayerGUI.getPlaylistNameLabel().setVisible(true);
            musicPlayerGUI.toggleShuffleButton(true);

        } else {
            musicPlayerGUI.getPlaylistNameLabel().setVisible(false);
            musicPlayerGUI.toggleShuffleButton(false);
        }
    }

}