package com.example.chessserverspringboot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "games")
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "white_id")
    private User whitePlayer;

    @ManyToOne
    @JoinColumn(name = "black_id")
    private User blackPlayer;

    private String result; // "1-0", "0-1", "½-½"
    private String timeControl; // "3|2", "10|0"
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime finishedAt;

    @ElementCollection
    @CollectionTable(name = "moves", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "move")
    private List<String> moves = new ArrayList<>();

    public GameRecord() {}

    public void setWhitePlayer(User w) {
    }


    // getters/setters
}
