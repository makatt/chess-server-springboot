package com.example.chessserverspringboot.Repository;


import com.example.chessserverspringboot.Entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Integer> {

    @Query("SELECT m FROM Match m WHERE m.player1_id = :id OR m.player2_id = :id")
    List<Match> findByPlayer(@Param("id") int id);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.player1_id = :id OR m.player2_id = :id")
    int countByPlayer(@Param("id") int id);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.winner_id = :id")
    int countWins(@Param("id") int id);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.winner_id != :id AND m.winner_id IS NOT NULL AND (m.player1_id = :id OR m.player2_id = :id)")
    int countLosses(@Param("id") int id);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.winner_id IS NULL AND (m.player1_id = :id OR m.player2_id = :id)")
    int countDraws(@Param("id") int id);
}
