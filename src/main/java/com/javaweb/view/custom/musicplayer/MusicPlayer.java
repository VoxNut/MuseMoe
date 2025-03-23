package com.javaweb.view.custom.musicplayer;

import com.javaweb.enums.RepeatMode;
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
import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;

@Getter
@Setter
public class MusicPlayer extends PlaybackListener {
    // this will be used to update isPaused more synchronously
    private static final Object playSignal = new Object();

    // need reference so that we can update the gui in this class
    private MusicPlayerGUI musicPlayerGUI;

    // we will need a way to store our song's details, so we will be creating a song
    // class
    @Getter
    private Song currentSong;

    private LinkedList<Song> playlist;

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

    // constructor
    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
    }

    BufferedInputStream bufferedInputStream;
    FileInputStream fileInputStream;
    @Getter
    int calculatedFrame;

    public void loadSong(Song song) throws IOException {
        currentSong = song;
        playlist = null;

        // stop the song if possible
        if (!songFinished)
            stopSong();

        // play the current song if not null
        if (currentSong != null) {

            // update gui
            updateUI();
            if (repeatMode == RepeatMode.REPEAT_ONE) {
                repeatMode = RepeatMode.REPEAT_ALL;
                musicPlayerGUI.updateRepeatButtonIcon();
            }
            musicPlayerGUI.toggleShuffleButton(false);
            musicPlayerGUI.getHomePage().showMusicPlayerHeader();
            musicPlayerGUI.updatePlaylistName(null);
            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile) throws IOException {
        currentPlaylistIndex = 0;
        if (!songFinished)
            stopSong();

        playlist = new LinkedList<>();

        // store the paths from the text file into the playlist array list
        try {
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // reach each line from the text file and store the text into the songPath
            // variable
            String songPath;
            while ((songPath = bufferedReader.readLine()) != null) {
                // create song object based on song path
                Song song = new Song(songPath);

                // add to playlist array list
                playlist.add(song);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!playlist.isEmpty()) {
            // update current song to the first song in the playlist
            currentSong = playlist.get(0);
            // update gui
            updateUI();
            if (repeatMode == RepeatMode.REPEAT_ONE) {
                repeatMode = RepeatMode.REPEAT_ALL;
                musicPlayerGUI.updateRepeatButtonIcon();
            }
            musicPlayerGUI.toggleShuffleButton(true);
            musicPlayerGUI.getHomePage().showMusicPlayerHeader();
            musicPlayerGUI.updatePlaylistName(playlistFile);
            // start song
            playCurrentSong();
        } else {
            musicPlayerGUI.toggleShuffleButton(false);
            JOptionPane.showMessageDialog(musicPlayerGUI, "Playlist không tồn tại!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void pauseSong() throws IOException {
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
            sliderThread = null;
        }

        if (advancedPlayer != null) {
            if (!songFinished) {
                advancedPlayer.stop();
            }
            advancedPlayer.close();
            advancedPlayer = null;
        }
        if (fileInputStream != null) {
            fileInputStream.close();
        }
        if (bufferedInputStream != null) {
            bufferedInputStream.close();
        }
    }

    public void nextSong() throws IOException {

        // A single song
        if (playlist == null) {
            if (repeatMode == RepeatMode.NO_REPEAT) {
                return;
            } else if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                pressedNext = true;
                if (!songFinished)
                    stopSong();
                updateUI();
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                    musicPlayerGUI.updateRepeatButtonIcon();
                }
                playCurrentSong();
            }
        } else {
            // Hava a playlist
            pressedNext = true;
            if (!songFinished)
                stopSong();

            if (currentPlaylistIndex + 1 == playlist.size()) {
                if (repeatMode == RepeatMode.NO_REPEAT) {
                    File playlistDir = new File("src/main/java/com/javaweb/view/custom/musicplayer/playlist");
                    File[] playlistFiles = playlistDir.listFiles((dir, name) -> name.endsWith(".txt"));
                    if (playlistFiles != null && playlistFiles.length > 0) {
                        File randomPlaylist = playlistFiles[(int) (Math.random() * playlistFiles.length)];
                        loadPlaylist(randomPlaylist);
                    }
                } else if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                    currentPlaylistIndex = 0;
                    currentSong = playlist.get(currentPlaylistIndex);
                    updateUI();
                    if (repeatMode == RepeatMode.REPEAT_ONE) {
                        repeatMode = RepeatMode.REPEAT_ALL;
                        musicPlayerGUI.updateRepeatButtonIcon();
                    }
                    playCurrentSong();
                }
                // Random song
            } else {
                // increase current playlist index
                currentPlaylistIndex++;
                // update current song
                currentSong = playlist.get(currentPlaylistIndex);
                updateUI();
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                    musicPlayerGUI.updateRepeatButtonIcon();
                }
                // play the song
                playCurrentSong();
            }

        }

    }

    public void prevSong() throws IOException {
        // A single song
        if (playlist == null) {
            if (repeatMode == RepeatMode.NO_REPEAT) {
                return;
            } else if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                pressedPrev = true;
                if (!songFinished)
                    stopSong();
                updateUI();
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                    musicPlayerGUI.updateRepeatButtonIcon();
                }
                playCurrentSong();
            }
        } else {
            // Have a playlist
            pressedPrev = true;
            if (!songFinished)
                stopSong();

            // check to see if we have reached the head of the playlist
            if (currentPlaylistIndex == 0) {
                if (repeatMode == RepeatMode.NO_REPEAT) {
                    updateUI();
                    playCurrentSong();
                } else if (repeatMode == RepeatMode.REPEAT_ALL || repeatMode == RepeatMode.REPEAT_ONE) {
                    currentPlaylistIndex = playlist.size() - 1;
                    currentSong = playlist.get(currentPlaylistIndex);
                    updateUI();
                    if (repeatMode == RepeatMode.REPEAT_ONE) {
                        repeatMode = RepeatMode.REPEAT_ALL;
                        musicPlayerGUI.updateRepeatButtonIcon();
                    }
                    playCurrentSong();
                }
                // Random song
            } else {
                // increase current playlist index
                currentPlaylistIndex--;
                // update current song
                currentSong = playlist.get(currentPlaylistIndex);
                updateUI();
                if (repeatMode == RepeatMode.REPEAT_ONE) {
                    repeatMode = RepeatMode.REPEAT_ALL;
                    musicPlayerGUI.updateRepeatButtonIcon();
                }
                // play the song
                playCurrentSong();
            }

        }
    }

    public void playCurrentSong() {
        if (currentSong == null)
            return;
        try {

            fileInputStream = new FileInputStream(currentSong.getFilePath());
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            device = FactoryRegistry.systemRegistry().createAudioDevice();
            advancedPlayer = new AdvancedPlayer(bufferedInputStream, device);

            advancedPlayer.setPlayBackListener(this);
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.getHomePage().enablePauseButtonDisablePlayButton();
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
        // this method gets called when the song finishes or if the player gets closed
        System.out.println("Playback Finished!");
        if (isPaused) {
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        } else {
            // if the user pressed next or prev we don't need to execute the rest of the
            // code
            if (pressedNext || pressedPrev || pressedShuffle || pressedReplay)
                return;

            // when the song ends
            if (playlist == null) {
                if (repeatMode == RepeatMode.REPEAT_ONE || repeatMode == RepeatMode.REPEAT_ALL) {

                    currentTimeInMilli = 0;
                    currentFrame = 0;
                    musicPlayerGUI.updatePlaybackSlider(currentSong);
                    musicPlayerGUI.getHomePage().updatePlaybackSlider(currentSong);

                    musicPlayerGUI.setPlaybackSliderValue(0);
                    musicPlayerGUI.getHomePage().setPlaybackSliderValue(0);
                    musicPlayerGUI.setVolumeSliderValue(0);

                    playCurrentSong();
                } else {
                    songFinished = true;
                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
                    musicPlayerGUI.getHomePage().enablePlayButtonDisablePauseButton();
                }
            } else {
                // last song in the playlist
                if (currentPlaylistIndex == playlist.size() - 1) {
                    if (repeatMode == RepeatMode.REPEAT_ALL) {
                        currentPlaylistIndex = 0;
                        currentSong = playlist.get(currentPlaylistIndex);
                        try {
                            updateUI();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        playCurrentSong();
                    } else if (repeatMode == RepeatMode.REPEAT_ONE) {
                        currentTimeInMilli = 0;
                        currentFrame = 0;
                        musicPlayerGUI.updatePlaybackSlider(currentSong);
                        musicPlayerGUI.getHomePage().updatePlaybackSlider(currentSong);

                        musicPlayerGUI.setPlaybackSliderValue(0);
                        musicPlayerGUI.getHomePage().setPlaybackSliderValue(0);
                        musicPlayerGUI.setVolumeSliderValue(0);

                        playCurrentSong();
                    } else {
                        songFinished = true;
                        musicPlayerGUI.enablePlayButtonDisablePauseButton();
                        musicPlayerGUI.getHomePage().enablePlayButtonDisablePauseButton();
                    }
                } else {

                    if (repeatMode == RepeatMode.REPEAT_ONE) {
                        currentTimeInMilli = 0;
                        currentFrame = 0;
                        musicPlayerGUI.updatePlaybackSlider(currentSong);
                        musicPlayerGUI.getHomePage().updatePlaybackSlider(currentSong);

                        musicPlayerGUI.setPlaybackSliderValue(0);
                        musicPlayerGUI.getHomePage().setPlaybackSliderValue(0);
                        musicPlayerGUI.setVolumeSliderValue(0);

                        playCurrentSong();
                    } else {
                        currentPlaylistIndex++;
                        currentSong = playlist.get(currentPlaylistIndex);
                        try {
                            updateUI();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        playCurrentSong();
                    }
                }
            }
        }
    }

    public void replayFiveSeconds() throws IOException {
        if (currentSong == null)
            return;

        pressedReplay = true;

        // Calculate new position
        long newPosition = currentTimeInMilli - 5000;
        if (newPosition < 0) {
            newPosition = 0;
        }

        // Convert milliseconds to frames
        int newFrame = (int) (newPosition * currentSong.getFrameRatePerMilliseconds());

        // Stop current playback
        if (!songFinished) {
            stopSong();
        }

        // Update current position
        currentTimeInMilli = (int) newPosition;
        currentFrame = newFrame;

        // Update UI
        musicPlayerGUI.setPlaybackSliderValue(newFrame);
        musicPlayerGUI.getHomePage().setPlaybackSliderValue(newFrame);

        // Restart playback from new position
        playCurrentSong();
    }

    public void shufflePlaylist() throws IOException {

        if (playlist == null || playlist.size() <= 1) {
            musicPlayerGUI.toggleShuffleButton(false);
            return;
        }

        pressedShuffle = true;

        if (!songFinished) {
            stopSong();
        }

        Collections.shuffle(playlist);
        currentPlaylistIndex = 0;
        currentSong = playlist.get(currentPlaylistIndex);

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
        playCurrentSong();

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
    }

}