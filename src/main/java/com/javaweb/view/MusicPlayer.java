package com.javaweb.view;

import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.view.mini_musicplayer.advertisement.AdvertisementManager;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerFacade;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerMediator;
import com.javaweb.view.user.UserSessionManager;
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
    private final MusicPlayerMediator mediator;

    // need reference so that we can update the gui in this class

    // we will need a way to store our song's details, so we will be creating a song
    // class
    @Getter
    @Setter
    private SongDTO currentSong;

    @Getter
    @Setter
    private PlaylistDTO currentPlaylist;

    // we will need to keep track the index we are in the playlist
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

    private final AdvertisementManager adManager;


    @Getter
    int calculatedFrame;

    @Getter
    private boolean havingAd;

    @Getter
    private RepeatMode repeatMode = RepeatMode.NO_REPEAT;


    // constructor
    public MusicPlayer() {
        this.adManager = AdvertisementManager.getInstance();
        this.mediator = MusicPlayerMediator.getInstance();
    }

    public void loadSong(SongDTO song) throws IOException {
        resetPlaybackPosition();
        // stop the song if possible
        stopSong();

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        //Check if Free user has meet the ads condition.
        if (adManager.shouldShowAd(getCurrentUser())) {
            // Store the original song URL before playing the ad
            if (song != null) {
                adManager.storeLastSong(song);
            }
            havingAd = true;
            //get random ad url
            String randomAdUrl = adManager.getAdvertisements()[(int) (Math.random() * adManager.getAdvertisements().length)];
            currentSong = CommonApiUtil.fetchSongByUrl(randomAdUrl);
            mediator.notifyAdOn();
        } else {
            havingAd = false;
            currentSong = song;
            mediator.notifyAdOff();

            //Log the play history only if the current song is not ad.
            CommonApiUtil.logPlayHistory(currentSong.getId());
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
            //Update MusicPlayerGUI
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
            fileInputStream = new FileInputStream(currentSong.getAudioFilePath());
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            device = FactoryRegistry.systemRegistry().createAudioDevice();
            advancedPlayer = new AdvancedPlayer(bufferedInputStream, device);

            advancedPlayer.setPlayBackListener(this);

            startMusicThread();
            startPlaybackSliderThread();


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
            sliderThread.interrupt();
            try {
                sliderThread.join(200);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        sliderThread = new Thread(() -> {
            try {
                // Store important values locally to prevent race conditions
                final SongDTO currentSongCopy = currentSong;
                if (currentSongCopy == null) return;

                final int totalFrames = currentSongCopy.getMp3File().getFrameCount();
                final double frameRate = currentSongCopy.getFrameRatePerMilliseconds();
                final long songDurationMs = currentSongCopy.getMp3File().getLengthInMilliseconds();

                // Handle pause state correctly (with timeout to prevent deadlock)
                if (isPaused) {
                    synchronized (playSignal) {
                        try {
                            playSignal.wait(5000); // Wait up to 5 seconds
                        } catch (InterruptedException e) {
                            return; // Exit if interrupted
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

                    // Only notify mediator if the thread isn't interrupted and we're still playing
                    if (!Thread.currentThread().isInterrupted() && !isPaused &&
                        !songFinished && advancedPlayer != null) {
                        // Use invokeAndWait to ensure UI updates happen synchronously
                        mediator.notifyPlaybackProgress(calculatedFrame, currentTimeInMilli);
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

        // Make it a daemon thread so it doesn't prevent JVM shutdown
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
        mediator.notifyPlaybackStarted();
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

            int totalFrames = currentSong.getMp3File().getFrameCount();
            int threshold = (int) (totalFrames * 0.95);

            //Naturally end
            if (calculatedFrame >= threshold) {
                mediator.notifyPlaybackPaused();
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
            MusicPlayerFacade.getInstance().loadSong(currentSong);
        } else {
            mediator.notifyPlaybackPaused();
            songFinished = true;
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
                MusicPlayerFacade.getInstance().loadSong(currentSong);

            } else if (repeatMode == RepeatMode.REPEAT_ONE) {
                // Repeat the current song
                MusicPlayerFacade.getInstance().loadSong(currentSong);

            } else {
                // End of playlist with no repeat
                mediator.notifyPlaybackPaused();
                songFinished = true;
            }
        } else {
            if (repeatMode == RepeatMode.REPEAT_ONE) {
                // Repeat the current song
                MusicPlayerFacade.getInstance().loadSong(currentSong);
            } else {
                try {
                    MusicPlayerFacade.getInstance().nextSong();
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
    }

    public void replayFiveSeconds() throws IOException {
        if (havingAd) return;
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


        loadSong(currentSong);

    }

    public void setVolume(float gain) {
        if (this.volumeControl == null) {
            Class<JavaSoundAudioDevice> clazz = JavaSoundAudioDevice.class;
            Field[] fields = clazz.getDeclaredFields();
            try {
                SourceDataLine source;
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
    }

    private void updateGUI() {
        mediator.notifySongLoaded(currentSong);
        if (currentPlaylist != null) {
            mediator.notifyPlaylistLoaded(currentPlaylist);
        }
    }

    private UserDTO getCurrentUser() {
        return UserSessionManager.getInstance().getCurrentUser();
    }


}