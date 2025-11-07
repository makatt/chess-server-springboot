package com.example.chessserverspringboot.repository;

import com.example.chessserverspringboot.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    List<GameRecord> findByWhitePlayerOrBlackPlayer(User white, User black);
}