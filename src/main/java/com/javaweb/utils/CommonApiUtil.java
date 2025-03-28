package com.javaweb.utils;

import com.javaweb.App;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.client.client_service.*;
import com.javaweb.utils.client.impl.ApiServiceFactory;

import java.util.List;
import java.util.Set;

public class CommonApiUtil {

    private static UserApiClient getUserApiClient() {
        return App.getBean(ApiServiceFactory.class).createUserApiClient();
    }

    private static SongApiClient getSongApiClient() {
        return App.getBean(ApiServiceFactory.class).createSongApiClient();
    }

    private static PlaylistApiClient getPlaylistApiClient() {
        return App.getBean(ApiServiceFactory.class).createPlaylistApiClient();
    }

    private static UserDownloadApiClient getUserDownloadApiClient() {
        return App.getBean(ApiServiceFactory.class).createUserDownloadApiClient();
    }

    private static SongLikesApiClient getSongLikesApiClient() {
        return App.getBean(ApiServiceFactory.class).createSonglikesdApiClient();
    }

    // USER
    public static Set<UserDTO> fetchAllUsersBaseOnRole(RoleType role) {
        return getUserApiClient().fetchAllUsersBaseOnRole(role);
    }

    public static void updateLastLoginTime() {
        getUserApiClient().updateLastLoginTime();
    }

    public static UserDTO fetchUserByUsername(String username) {
        return getUserApiClient().fetchUserByUsername(username);
    }

    public static boolean updateUserPassword(Long id, String password) {
        return getUserApiClient().updateUserPassword(id, password);
    }

    public static boolean createNewUser(String username, String password, String email) {
        return getUserApiClient().createNewUser(username, password, email);
    }

    public static UserDTO fetchUserById(Long id) {
        return getUserApiClient().fetchUserById(id);
    }

    public static UserDTO fetchUserByPhone(String phone) {
        return getUserApiClient().fetchUserByPhone(phone);
    }

    public static UserDTO fetchUserByEmail(String email) {
        return getUserApiClient().fetchUserByEmail(email);
    }

    // Song
    public static SongDTO fetchSongByTitle(String title) {
        return getSongApiClient().fetchSongByTitle(title);
    }

    public static List<SongDTO> findSongsLike(String title) {
        return getSongApiClient().findSongsLike(title);
    }

    public static SongDTO fetchSongByUrl(String songUrl) {
        return getSongApiClient().findSongByUrl(songUrl);
    }

    public static List<SongDTO> fetchAllSongs() {
        return getSongApiClient().findAllSongs();
    }

    public static List<SongDTO> fetchUserDownloadedSongs() {
        return getUserDownloadApiClient().findUserDownloadedSongs();
    }

    //SongLikes

    public static boolean createSongLikes(Long songId) {
        return getSongLikesApiClient().createNewSongLikes(songId);
    }

    public static boolean checkSongLiked(Long songId) {
        return getSongLikesApiClient().checkSongLiked(songId);
    }

    // Playlist
    public static List<PlaylistDTO> fetchPlaylistByUserId() {
        return getPlaylistApiClient().findPlaylistByUserId();
    }

    public static List<PlaylistDTO> fetchAllPlaylists() {
        return getPlaylistApiClient().findAllPlaylists();
    }


}