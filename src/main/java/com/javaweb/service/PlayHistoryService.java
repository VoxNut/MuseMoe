package com.javaweb.service;

import com.javaweb.model.dto.SongDTO;

import java.util.List;

public interface PlayHistoryService {
    boolean createNewPlayHistory(Long songId);

    List<SongDTO> fetchRecentPlayHistory(Integer limit);

    List<Long> fetchRecentPlayedSongIds(Integer limit);

    boolean clearPlayHistoryBySongs(List<Long> songIds);

    boolean clearAllPlayHistory();

}
