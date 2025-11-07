package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.model.GameRecord;
import com.example.chessserverspringboot.model.User;
import com.example.chessserverspringboot.repository.GameRecordRepository;
import com.example.chessserverspringboot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameRecordService {
    private final GameRecordRepository repo;
    private final UserRepository userRepo;

    public GameRecordService(GameRecordRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    public GameRecord saveGame(String white, String black, String result,
                               String timeControl, List<String> moves) {
        User w = userRepo.findByUsername(white);
        User b = userRepo.findByUsername(black);
        GameRecord record = new GameRecord();
        record.setWhitePlayer(w);
        record.setBlackPlayer(b);
        record.setResult(result);
        record.setTimeControl(timeControl);
        record.setMoves(moves);
        record.setFinishedAt(java.time.LocalDateTime.now());
        return repo.save(record);
    }

    public List<GameRecord> getUserGames(String username) {
        User u = userRepo.findByUsername(username);
        return repo.findByWhitePlayerOrBlackPlayer(u, u);
    }
}
