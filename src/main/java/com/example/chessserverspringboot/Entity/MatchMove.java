package com.example.chessserverspringboot.Entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_moves", schema = "chess")
public class MatchMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "move_id")
    private Integer id;

    private Integer match_id;
    private Integer player_id;

    private Integer move_number;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String move_data;

    private String fen_position;

    private LocalDateTime created_at = LocalDateTime.now();

    public void setMatch_id(Integer id) { this.match_id = id; }
    public void setPlayer_id(Integer id) { this.player_id = id; }
    public void setMove_number(Integer n) { this.move_number = n; }
    public void setMove_data(String d) { this.move_data = d; }
    public void setFen_position(String fen) { this.fen_position = fen; }
}
