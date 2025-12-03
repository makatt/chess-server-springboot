package com.example.chessserverspringboot.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches", schema = "chess")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Integer id;

    private Integer player1_id;
    private Integer player2_id;
    private Integer winner_id;

    private LocalDateTime started_at = LocalDateTime.now();
    private LocalDateTime ended_at;

    private String final_fen;
    private String end_reason;

    public Integer getId() { return id; }

    public void setPlayer1_id(Integer id) { this.player1_id = id; }
    public void setPlayer2_id(Integer id) { this.player2_id = id; }
    public void setWinner_id(Integer id) { this.winner_id = id; }
    public void setEnded_at(LocalDateTime t) { this.ended_at = t; }
    public void setFinal_fen(String fen) { this.final_fen = fen; }
    public void setEnd_reason(String r) { this.end_reason = r; }
}
