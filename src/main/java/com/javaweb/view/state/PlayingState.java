//package com.javaweb.view.state;
//
//import com.javaweb.model.dto.SongDTO;
//import com.javaweb.view.MusicPlayer;
//
//import java.io.IOException;
//
//public class PlayingState implements PlayerState {
//    private final MusicPlayer player;
//
//    public PlayingState(MusicPlayer player) {
//        this.player = player;
//    }
//
//    @Override
//    public void play() {
//    }
//
//    @Override
//    public void pause() throws IOException {
//        if (player.isHavingAd()) return;
//
//        player.pauseSong();
//        player.setCurrentState(new PausedState(player));
//        player.getMediator().notifyPlaybackPaused();
//    }
//
//    @Override
//    public void stop() throws IOException {
//        player.stopPlayback();
//        player.setCurrentState(new StoppedState(player));
//    }
//
//    @Override
//    public void next() throws IOException {
//        if (player.isHavingAd()) return;
//
//        player.setPressedNext(true);
//        player.handleNextSong();
//    }
//
//    @Override
//    public void previous() throws IOException {
//        if (player.isHavingAd()) return;
//
//        player.setPressedPrev(true);
//        player.handlePreviousSong();
//    }
//
//    @Override
//    public void loadSong(SongDTO song) throws IOException {
//        player.stopPlayback();
//        player.doLoadSong(song);
//    }
//
//    @Override
//    public void replayFiveSeconds() throws IOException {
//        if (player.isHavingAd() || player.getCurrentSong() == null) return;
//
//        player.setPressedReplay(true);
//
//        long newPosition = player.getCurrentTimeInMilli() - 5000;
//        if (newPosition < 0) newPosition = 0;
//
//        player.stopPlayback();
//        player.setCurrentTimeInMilli((int) newPosition);
//        player.setCurrentFrame((int) (newPosition * player.getCurrentSong().getFrameRatePerMilliseconds()));
//        player.playCurrentSong();
//    }
//
//    @Override
//    public void shufflePlaylist() throws IOException {
//        if (player.getCurrentPlaylist() == null ||
//                player.getCurrentPlaylist().size() <= 1) {
//            return;
//        }
//
//        player.setPressedShuffle(true);
//        player.handleShufflePlaylist();
//    }
//
//    @Override
//    public void handlePlaybackFinished(int frame) {
//        // Handle natural end of song
//        if (frame >= player.getCurrentSong().getFrame() * 0.95) {
//            try {
//                player.getMediator().notifyPlaybackPaused();
//                player.getAdManager().updateUserPlayCounter(player.getCurrentUser());
//
//                if (player.getCurrentPlaylist() == null) {
//                    player.handleSingleSongCompletion();
//                } else {
//                    player.handlePlaylistSongCompletion();
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    @Override
//    public String getName() {
//        return "Playing";
//    }
//}