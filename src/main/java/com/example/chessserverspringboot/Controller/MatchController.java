package com.example.chessserverspringboot.Controller;

import com.example.chessserverspringboot.Entity.Match;
import com.example.chessserverspringboot.Entity.MatchMove;
import com.example.chessserverspringboot.Repository.MatchMovesRepository;
import com.example.chessserverspringboot.Repository.MatchRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*") // позволяет Android подключаться
public class MatchController {

    private final MatchRepository matchRepo;
    private final MatchMovesRepository movesRepo;

    public MatchController(MatchRepository matchRepo,
                           MatchMovesRepository movesRepo) {
        this.matchRepo = matchRepo;
        this.movesRepo = movesRepo;
    }

    /** 1️⃣ Все матчи игрока */
    @GetMapping("/player/{playerId}")
    public List<Match> getMatchesByPlayer(@PathVariable int playerId) {
        return matchRepo.findByPlayer(playerId);
    }

    /** 2️⃣ Ходы конкретного матча */
    @GetMapping("/{matchId}/moves")
    public List<MatchMove> getMatchMoves(@PathVariable int matchId) {
        return movesRepo.findByMatchId(matchId);
    }

    /** 3️⃣ Полная информация о матче */
    @GetMapping("/{matchId}")
    public Match getMatchInfo(@PathVariable int matchId) {
        return matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
    }

    /** 4️⃣ Статистика игрока */
    @GetMapping("/player/{playerId}/stats")
    public PlayerStats getPlayerStats(@PathVariable int playerId) {
        int total = matchRepo.countByPlayer(playerId);
        int wins = matchRepo.countWins(playerId);
        int losses = matchRepo.countLosses(playerId);
        int draws = matchRepo.countDraws(playerId);

        return new PlayerStats(total, wins, losses, draws);
    }

    public record PlayerStats(int total, int wins, int losses, int draws) {}
}
