package com.example.chessserverspringboot.controller;

import com.example.chessserverspringboot.model.GameRecord;
import com.example.chessserverspringboot.service.GameRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
@CrossOrigin
public class GameRecordController {
    private final GameRecordService service;

    public GameRecordController(GameRecordService service) { this.service = service; }

    @GetMapping("/{username}")
    public List<GameRecord> getGames(@PathVariable String username) {
        return service.getUserGames(username);
    }
}
