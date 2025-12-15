package com.example.chessserverspringboot.Registration;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profile", schema = "chess")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    private String avatar_url;
    private String bio;

    private Integer rating = 1000;
    private Integer games_played = 0;
    private Integer games_won = 0;
    private Integer games_lost = 0;

    public Integer getUserId() { return userId; }
    public String getAvatar_url() { return avatar_url; }
    public String getBio() { return bio; }
    public Integer getRating() { return rating; }
    public Integer getGames_played() { return games_played; }
    public Integer getGames_won() { return games_won; }
    public Integer getGames_lost() { return games_lost; }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }
    public void setBio(String bio) { this.bio = bio; }
}
