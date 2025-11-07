package com.example.chessserverspringboot.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password; // можно позже зашифровать BCrypt

    private int rating = 1200;

    @OneToMany(mappedBy = "whitePlayer", cascade = CascadeType.ALL)
    private List<GameRecord> gamesAsWhite = new ArrayList<>();

    @OneToMany(mappedBy = "blackPlayer", cascade = CascadeType.ALL)
    private List<GameRecord> gamesAsBlack = new ArrayList<>();

    public User() {}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String GetUsername(){
        return username
    }
}

