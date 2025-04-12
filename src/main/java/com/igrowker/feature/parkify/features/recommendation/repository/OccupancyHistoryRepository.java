package com.igrowker.feature.parkify.features.recommendation.repository;

import com.igrowker.feature.parkify.features.recommendation.entities.OccupancyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OccupancyHistoryRepository extends JpaRepository<OccupancyHistory, Long> {
    @Query("SELECT o FROM OccupancyHistory o WHERE o.timestamp >= :since")
    List<OccupancyHistory> findRecentHistory(@Param("since") LocalDateTime since);
}
