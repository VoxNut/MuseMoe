package com.javaweb.view.mini_musicplayer.advertisement;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class AdvertisementManager {

    private static AdvertisementManager instance;
    private final Map<Long, Integer> userPlayCounter = new HashMap<>();
    private SongDTO lastSongDTO;
    private final String[] advertisements = {
            AppConstant.MUSE_MOE_AD,
            AppConstant.STUDY_SMART_AD,
            AppConstant.BREW_SPOT_AD,
            AppConstant.INSPIRATION_AD
    };

    private AdvertisementManager() {

    }

    public static synchronized AdvertisementManager getInstance() {
        if (instance == null) {
            instance = new AdvertisementManager();
        }
        return instance;
    }

    public boolean shouldShowAd(UserDTO user) {
        // Skip ads for premium, admin or artist users
        Set<String> roles = user.getRoles();
        if (roles.contains(AppConstant.PREMIUM_ROLE) ||
                roles.contains(AppConstant.ADMIN_ROLE) ||
                roles.contains(AppConstant.ARTIST_ROLE)) {
            return false;
        }


        return userPlayCounter.get(user.getId()) == AppConstant.SONGS_BEFORE_AD;
    }

    public void updateUserPlayCounter(UserDTO user) {
        Long userId = user.getId();
        int playCount = userPlayCounter.getOrDefault(userId, 0) + 1;
        userPlayCounter.put(userId, playCount);
    }

    public void resetUserCounter(Long userId) {
        userPlayCounter.put(userId, 0);
    }

    public void storeLastSong(SongDTO songDTO) {
        this.lastSongDTO = songDTO;
    }

}

