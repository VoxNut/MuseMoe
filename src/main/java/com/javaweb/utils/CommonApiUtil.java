package com.javaweb.utils;

import com.javaweb.App;
import com.javaweb.client.client_service.*;
import com.javaweb.client.impl.ApiServiceFactory;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;

import java.util.List;
import java.util.Set;

//Facade Design Pattern
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

    private static PlayHistoryApiClient getPlayHistoryApiClient() {
        return App.getBean(ApiServiceFactory.class).createPlayHistoryApiClient();
    }

    private static SearchHistoryApiClient getSearchHistoryApiClient() {
        return App.getBean(ApiServiceFactory.class).createSearchHistoryApiClient();
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

    public static UserDTO fetchCurrentUser() {
        return getUserApiClient().fetchCurrentUser();
    }

    public static UserDTO fetchUserByPhone(String phone) {
        return getUserApiClient().fetchUserByPhone(phone);
    }

    public static UserDTO fetchUserByEmail(String email) {
        return getUserApiClient().fetchUserByEmail(email);
    }

    // Song

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

    public static List<SongDTO> fetchRecentPlayHistory(int limit) {
        return getPlayHistoryApiClient().findRecentPlayHistory(limit);
    }

    public static List<SongDTO> searchSongs(String query) {
        return findSongsLike(query);
    }

    //SongLikes

    public static boolean createSongLikes(Long songId) {
        return getSongLikesApiClient().createNewSongLikes(songId);
    }

    public static boolean deleteSongLikes(Long songId) {
        return getSongLikesApiClient().deleteSongLikes(songId);
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

    // Play_History
    public static boolean logPlayHistory(Long songId) {
        return getPlayHistoryApiClient().createNewPlayHistory(songId);
    }

    public static boolean clearPlayHistoryBySongs(List<Long> songIds) {
        return getPlayHistoryApiClient().clearPlayHistoryBySongs(songIds);
    }

    public static boolean clearAllPlayHistory() {
        return getPlayHistoryApiClient().clearAllPlayHistory();
    }

    //Search_History
    public static boolean logSearchHistory(Long songId, String searchTerm) {
        return getSearchHistoryApiClient().logSearchHistory(songId, searchTerm);
    }

    public static List<SongDTO> fetchRecentSearchHistory(int limit) {
        return getSearchHistoryApiClient().findRecentSearchHistory(limit);
    }

    public static List<String> fetchRecentSearchTerms(int limit) {
        return getSearchHistoryApiClient().findRecentSearchTerms(limit);
    }

    public static boolean clearSearchHistoryBySongs(List<Long> songIds) {
        return getSearchHistoryApiClient().clearSearchHistoryBySongs(songIds);
    }

    public static boolean clearAllSearchHistory() {
        return getSearchHistoryApiClient().clearAllSearchHistory();
    }
}