package com.javaweb.client.client_service;

import com.javaweb.model.dto.SongDTO;

import java.util.List;

public interface PlayHistoryApiClient {
    Boolean createNewPlayHistory(Long songId);

    List<SongDTO> findRecentPlayHistory(int limit);

    Boolean clearPlayHistoryBySongs(List<Long> songIds);

    Boolean clearAllPlayHistory();

}
