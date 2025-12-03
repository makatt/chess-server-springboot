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

    // ---- GETTERS ----

    public Integer getId() { return id; }
    public Integer getPlayer1_id() { return player1_id; }
    public Integer getPlayer2_id() { return player2_id; }
    public Integer getWinner_id() { return winner_id; }

    public LocalDateTime getStarted_at() { return started_at; }
    public LocalDateTime getEnded_at() { return ended_at; }

    public String getFinal_fen() { return final_fen; }
    public String getEnd_reason() { return end_reason; }

    // ---- SETTERS ----

    public void setPlayer1_id(Integer id) { this.player1_id = id; }
    public void setPlayer2_id(Integer id) { this.player2_id = id; }
    public void setWinner_id(Integer id) { this.winner_id = id; }

    public void setEnded_at(LocalDateTime t) { this.ended_at = t; }
    public void setFinal_fen(String fen) { this.final_fen = fen; }
    public void setEnd_reason(String r) { this.end_reason = r; }
}
