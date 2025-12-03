package com.example.chessserverspringboot.Repository;


import com.example.chessserverspringboot.Entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Integer> {}
