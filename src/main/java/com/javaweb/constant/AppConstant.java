package com.javaweb.constant;

import java.awt.*;

public class AppConstant {
    //COLOR
    public static final Color BACKGROUND_COLOR = Color.decode("#1A1A1A"); // Deep black for a sleek background
    public static final Color TEXT_COLOR = Color.decode("#F8E1E9"); // Soft pinkish-white inspired by cherry blossoms
    public static final Color TEXTFIELD_BACKGROUND_COLOR = Color.decode("#5A4A6F");
    public static final Color NAVBAR_BACKGROUND_COLOR = Color.decode("#3A2E2A"); // Dark brown inspired by tree branches
    public static final Color BUTTON_BACKGROUND_COLOR = Color.decode("#2E2D3B"); // Dark gray with a purple hint to tie into MuseMoe's theme
    public static final Color BUTTON_TEXT_COLOR = Color.decode("#FFFFFF"); // Glowing white for contrast
    public static final Color ACTIVE_BUTTON_TEXT_COLOR = Color.decode("#3A2E2A"); // Dark brown for active button text
    public static final Color ACTIVE_BUTTON_BACKGROUND_COLOR = Color.decode("#F8E1E9"); // Cherry blossom tone for active buttons
    public static final Color HEADER_BORDER_COLOR = Color.decode("#FFFFFF"); // Glowing white for header borders
    public static final Color TABLE_TEXT = Color.decode("#F8E1E9"); // Soft pinkish-white for table text
    public static final Color DISABLED_BACKGROUND_COLOR = Color.decode("#555555"); // Muted gray for disabled elements
    public static final Color ACTIVE_BACKGROUND_COLOR = Color.decode("#F8E1E9"); // Cherry blossom tone for active backgrounds
    public static final Color BORDER_COLOR = Color.decode("#3A2E2A"); // Dark brown for borders
    public static final Color HEADER_BACKGROUND_COLOR = Color.decode("#2A2A2A"); // Slightly lighter dark tone for headers
    public static final Color POPUP_BACKGROUND_COLOR = Color.decode("#F8E1E9"); // Cherry blossom tone for popups
    public static final Color SIDEBAR_BACKGROUND_COLOR = Color.decode("#3A2E2A"); // Matching the navbar for consistency
    public static final Color PENDING = Color.decode("#FFFFFF"); // Glowing white for pending elements
    public static final Color DISABLE_BACKGROUND_BUTTON = Color.decode("#555555"); // Unchanged, muted gray for disabled buttons
    public static final Color DISABLE_TEXT_BUTTON = Color.decode("#777777"); // Muted gray for disabled button text


    //ROLE
    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String FREE_ROLE = "ROLE_FREE";
    public static final String PREMIUM_ROLE = "ROLE_PREMIUM";
    public static final String ARTIST_ROLE = "ROLE_ARTIST";


    //HOMEPAGE
    public static final String HOME_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/home-icon.png";
    public static final String MINIPLAYER_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/miniplayer-icon.png";
    public static final String HISTORY_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/history-icon.jpg";
    public static final String MUSIC_NOTE_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/music-note-icon.png";
    public static final String CLOCK_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/clock-icon.png";
    public static final String DEFAULT_ARTIST_PROFILE_PATH = "src/main/java/com/javaweb/view/imgs/artist_profile/default-profile-without-text.png";
    public static final String GO_BACK_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/go-back-icon.png";
    public static final String GO_FORWARD_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/go-forward-icon.png";
    public static final String PLAYLIST_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/playlist-icon.png";
    public static final String ARTIST_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/music-artist.png";


    //SALE PAGE
    public static final String CREATE_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/create-icon.png";
    public static final String DELETE_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/delete-icon.png";
    public static final String DESELECT_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/deselected-icon.png";
    public static final String LOOKUP_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/lookup-icon.png";
    //DISCOUNT PAGE
    public static final String DISCOUNT_PATH = "src/main/java/com/javaweb/view/imgs/icon/discount-img.png";
    public static final String CALENDAR_PATH = "src/main/java/com/javeaweb/view/imgs/icon/calendar-icon.png";

    //MUSIC PLAYER
    public static final String PLAY_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/play-icon.png";
    public static final String PAUSE_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/pause-icon.png";
    public static final String NEXT_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/next-icon.png";
    public static final String SPEAKER_ICON = "src/main/java/com/javaweb/view/imgs/icon/speaker-icon.png";
    public static final String SPEAKER_0_ICON = "src/main/java/com/javaweb/view/imgs/icon/0v-speaker-icon.png";
    public static final String SPEAKER_25_ICON = "src/main/java/com/javaweb/view/imgs/icon/25v-speaker-icon.png";
    public static final String SPEAKER_75_ICON = "src/main/java/com/javaweb/view/imgs/icon/75v-speaker-icon.png";
    public static final String PREVIOUS_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/previous-icon.png";

    public static final String DEFAULT_COVER_PATH = "src/main/java/com/javaweb/view/mini_musicplayer/album_cover/record2.png";
    public static final String SHUFFLE_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/shuffle-icon.png";
    public static final String REPLAY_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/replay-icon.png";
    public static final String HEART_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/heart-icon.png";
    public static final String HEART_OUTLINE_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/heart-outline.png";
    public static final String REPEAT_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/repeat-icon.png";
    public static final String ON_REPEAT_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/on-repeat-icon.png";
    public static final String REPEAT_1_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/repeat-1-icon.png";
    public static final String LYRICS_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/lyrics-icon.png";
    public static final Color MUSIC_PLAYER_TEXT_COLOR = Color.decode("#FFFFFF");

    //LOGO
    public static final String MUSE_MOE_LOGO_PATH = "src/main/java/com/javaweb/view/imgs/logo/muse_moe_no_bg.png";


    //ICON
    public static final String SUCCESS_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/success-icon.png";
    public static final String INFORMATION_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/information-icon.png";
    public static final String WARNING_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/warning-icon.jpg";
    public static final String ERROR_ICON_PATH = "src/main/java/com/javaweb/view/imgs/icon/error-icon.png";


    //FONT
    public static final String FONT_PATH = "src/main/java/com/javaweb/view/fonts/IBMPlexSansJP-Regular.ttf";
    public static final String SPOTIFY_FONT_PATH = "src/main/java/com/javaweb/view/fonts/IBMPlexSansJP-Regular.ttf";


    //SIZE
    public static final Dimension FILE_CHOOSER_SIZE = new Dimension(1100, 600);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(300, 30);

    //MISCELLANEOUS
    public static final String CHANGE_PASSWORD_FAIL = "change_password_fail";
    public static final String DEFAULT_PASSWORD = "123321";
    public static final String DEFAULT_USER_AVT_PATH = "src/main/java/com/javaweb/view/imgs/avatars/anonymous.png";
    public static final String GIF_PATH = "src/main/java/com/javaweb/view/imgs/back_ground/LatteLiteratureBackGround.gif";
    public static final int SONGS_BEFORE_AD = 10;
    public static final int RECENT_SEARCHED_SONG_LIMIT = 10;

    //ADS
    public static final String MUSE_MOE_AD = "src/main/java/com/javaweb/view/mini_musicplayer/advertisement/museMoe.mp3";
    public static final String STUDY_SMART_AD = "src/main/java/com/javaweb/view/mini_musicplayer/advertisement/studySmart.mp3";
    public static final String BREW_SPOT_AD = "src/main/java/com/javaweb/view/mini_musicplayer/advertisement/theBrewSpot.mp3";
    public static final String INSPIRATION_AD = "src/main/java/com/javaweb/view/mini_musicplayer/advertisement/truyenCamHung.mp3";
}
