package com.javaweb.utils;

import com.javaweb.App;
import com.javaweb.client.client_service.*;
import com.javaweb.client.impl.ApiServiceFactory;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.*;
import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.model.request.SongRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private static AlbumApiClient getAlbumApiClient() {
        return App.getBean(ApiServiceFactory.class).createAlbumApiClient();
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

    private static UserArtistFollowApiClient getUserArtistFollowClient() {
        return App.getBean(ApiServiceFactory.class).createUserArtistFollowApiClient();
    }

    private static ArtistApiClient getArtistApiClient() {
        return App.getBean(ApiServiceFactory.class).createArtistApiClient();
    }

    private static TagApiClient getTagApiClient() {
        return App.getBean(ApiServiceFactory.class).createTagApiClient();
    }

    // USER
    public static Set<UserDTO> fetchAllUsersBaseOnRole(RoleType role) {
        return getUserApiClient().fetchAllUsersBaseOnRole(role);
    }

    public static List<UserDTO> fetchUsersByDateRange(Date from, Date to) {
        return getUserApiClient().fetchAllUsers().stream().filter(userDTO -> {
            Date userDate = userDTO.getCreatedAt();
            if (userDate != null) {
                return userDate.after(from) && userDate.before(to);
            }
            return false;
        }).toList();
    }

    public static List<UserDTO> fetchAllUsers() {
        return getUserApiClient().fetchAllUsers();
    }

    public static boolean checkUserArtist(Long currentArtistId) {
        return getUserApiClient().checkUserArtist(currentArtistId);
    }

    public static boolean upgradeUser(RoleType roleType) {
        return getUserApiClient().upgradeUser(roleType);
    }

    public static boolean upgradeUserToArtist(String stageName, String bio, MultipartFile profilePicture) {
        return getArtistApiClient().createArtist(stageName, bio, profilePicture);
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

    public static boolean createNewUser(String username, String fullName, String password, String email) {
        return getUserApiClient().createNewUser(username, fullName, password, email);
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

    // User Artist Follow

    public static boolean unfollowArtist(Long artistId) {
        return getUserArtistFollowClient().unfollowArtist(artistId);
    }

    public static boolean followArtist(Long artistId) {
        return getUserArtistFollowClient().followArtist(artistId);
    }

    // Song
    public static List<SongDTO> findSongsLike(String title) {
        return getSongApiClient().findSongsLike(title);
    }

    public static List<SongDTO> fetchPopularTracksByArtistId(Long artistId) {
        return getSongApiClient().fetchPopularTracksByArtistId(artistId);
    }

    public static List<SongDTO> findTopSongByPlayCount(Integer limit) {
        return getSongApiClient().findTopByPlayCount(limit);
    }

    public static boolean createSongsForAlbum(SongRequestDTO songRequestDTO) {
        return getSongApiClient().createSongs(songRequestDTO);
    }

    public static boolean createSong(SongRequestDTO songRequestDTO) {
        return getSongApiClient().createSong(songRequestDTO);
    }

    public static List<SongDTO> fetchSongsByArtist(String artistName, int limit) {
        return getSongApiClient().fetchSongsByArtist(artistName, limit);
    }

    public static SongDTO fetchSongByUrl(String songUrl) {
        return getSongApiClient().findSongByUrl(songUrl);
    }

    public static List<SongDTO> fetchAllSongs() {
        return getSongApiClient().findAllSongs();
    }

    public static List<SongDTO> searchSongs(String query) {
        return getSongApiClient().search(query, 20);
    }

    public static List<SongDTO> fetchRecommendedSongs(int limit) {
        return getSongApiClient().fetchRecommendedSongs(limit);
    }

    public static SongDTO fetchSongByGoogleDriveId(String googleDriveId) {
        return getSongApiClient().fetchSongByGoogleDriveId(googleDriveId);
    }

    public static List<SongDTO> fetchUserDownloadedSongs() {
        return getUserDownloadApiClient().findUserDownloadedSongs();
    }

    public static boolean createUserDownload(SongDTO songId) {
        return getUserDownloadApiClient().createUserDownload(songId);
    }

    public static List<SongDTO> fetchRecentPlayHistory(int limit) {
        return getPlayHistoryApiClient().findRecentPlayHistory(limit);
    }


    // Tag
    public static Map<String, Integer> fetchTopTags(int limit) {
        return getTagApiClient().fetchTopTags(limit);
    }

    public static List<TagDTO> fetchAllTags() {
        return getTagApiClient().fetchAllTags();
    }

    public static List<TagDTO> findTagsBySongId(Long id) {
        return getTagApiClient().findTagsBySongId(id);
    }


    public static SongDTO fetchSongById(Long id) {
        return getSongApiClient().fetchSongById(id);
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


    public static List<SongLikesDTO> findAllSongLikes() {
        return getSongLikesApiClient().findAllSongLikesByUser();
    }

    // Playlist
    public static List<PlaylistDTO> fetchPlaylistByUserId() {
        return getPlaylistApiClient().findPlaylistByUserId();
    }

    public static List<PlaylistDTO> fetchAllPlaylists() {
        return getPlaylistApiClient().findAllPlaylists();
    }

    public static PlaylistDTO createPlaylist(String name, Long songId) {
        return getPlaylistApiClient().createPlaylist(name, songId);
    }

    public static List<PlaylistDTO> searchPlaylists(String query) {
        return getPlaylistApiClient().searchPlaylists(query, 20);
    }

    public static boolean addSongToPlaylist(PlaylistDTO playlistDTO) {
        return addSongsToPlaylist(playlistDTO);
    }

    public static boolean addSongsToPlaylist(PlaylistDTO playlistDTO) {
        return getPlaylistApiClient().addSongsToPlaylist(playlistDTO);
    }

    // Album
    public static List<AlbumDTO> searchAlbums(String query) {
        return getAlbumApiClient().searchAlbums(query, 20);
    }

    public static List<AlbumDTO> fetchRecommendedAlbums(int limit) {
        return getAlbumApiClient().getRecommendedAlbums(limit);
    }

    public static AlbumDTO fetchAlbumById(Long albumId) {
        return getAlbumApiClient().getAlbumById(albumId);
    }


    public static AlbumDTO fetchAlbumContainsThisSong(Long songId) {
        return getAlbumApiClient().getAlbumContainsThisSong(songId);
    }

    public static List<AlbumDTO> fetchAlbumsByArtistId(Long artistId) {
        return getAlbumApiClient().getAlbumsByArtistId(artistId);
    }

    public static AlbumDTO createAlbum(AlbumRequestDTO albumRequestDTO) {
        return getAlbumApiClient().createAlbum(albumRequestDTO);
    }

    public static List<AlbumDTO> fetchAllAlbums() {
        return getAlbumApiClient().findAllAlbums();
    }

    // Artist
    public static List<ArtistDTO> searchArtists(String query) {
        return getArtistApiClient().searchArtists(query, 20);
    }


    public static List<ArtistDTO> findArtistsBySongId(Long songId) {
        return getArtistApiClient().findArtistsBySongId(songId);
    }

    public static boolean checkArtistFollowed(Long artistId) {
        return getArtistApiClient().checkArtistFollowed(artistId);
    }

    public static ArtistDTO findArtistById(Long artistId) {
        return getArtistApiClient().findArtistById(artistId);
    }

    public static List<ArtistDTO> fetchAllArtists() {
        return getArtistApiClient().findAllArtists();
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


    public static List<ArtistDTO> fetchFollowedArtists() {
        return getUserArtistFollowClient().findFollowedArtists();
    }


}