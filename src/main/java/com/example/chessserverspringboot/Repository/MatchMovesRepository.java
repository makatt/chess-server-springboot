package com.example.chessserverspringboot.Repository;


import com.example.chessserverspringboot.Entity.MatchMove;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchMovesRepository extends JpaRepository<MatchMove, Integer> {}

