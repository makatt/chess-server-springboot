package com.example.chessserverspringboot.Repository;


import com.example.chessserverspringboot.Entity.MatchMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchMovesRepository extends JpaRepository<MatchMove, Integer> {

    @Query("SELECT mv FROM MatchMove mv WHERE mv.match_id = :matchId ORDER BY mv.move_number ASC")
    List<MatchMove> findByMatchId(@Param("matchId") int matchId);
}

