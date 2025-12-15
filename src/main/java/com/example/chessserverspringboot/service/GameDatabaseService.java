package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.Entity.Match;
import com.example.chessserverspringboot.Entity.MatchMove;
import com.example.chessserverspringboot.Registration.UserProfile;
import com.example.chessserverspringboot.Registration.UserProfileRepository;
import com.example.chessserverspringboot.Repository.MatchRepository;
import com.example.chessserverspringboot.Repository.MatchMovesRepository;
import com.example.chessserverspringboot.websocket.GameSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GameDatabaseService {

    @Autowired
    private MatchRepository matchRepo;

    @Autowired
    private MatchMovesRepository moveRepo;

    public int createMatch(int white, int black) {
        Match m = new Match();
        m.setPlayer1_id(white);
        m.setPlayer2_id(black);
        matchRepo.save(m);
        return m.getId();
    }

    @Autowired
    private ObjectMapper mapper;
    public void saveMove(int matchId, int playerId, int number, String moveData, String fen) {
        try {
            // ожидаем формат "e2e4"
            if (moveData == null || moveData.length() < 4) {
                throw new IllegalArgumentException("Invalid move format: " + moveData);
            }

            String from = moveData.substring(0, 2);
            String to = moveData.substring(2, 4);

            // JSON-структура хода
            ObjectNode json = mapper.createObjectNode();
            json.put("moveNumber", number);
            json.put("playerId", playerId);
            json.put("from", from);
            json.put("to", to);
            json.put("raw", moveData);
            json.put("timestamp", LocalDateTime.now().toString());

            MatchMove mv = new MatchMove();
            mv.setMatch_id(matchId);
            mv.setPlayer_id(playerId);
            mv.setMove_number(number);
            mv.setMove_data(mapper.writeValueAsString(json));
            mv.setFen_position(fen);

            moveRepo.save(mv);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Autowired
    private UserProfileRepository profileRepo;

    public void finishMatch(int matchId, Integer winnerId, String fen, String reason) {
        Match m = matchRepo.findById(matchId).orElseThrow();

        m.setWinner_id(winnerId);      // победитель
        m.setEnd_reason(reason);       // причина завершения
        m.setFinal_fen(fen);           // итоговая позиция
        m.setEnded_at(LocalDateTime.now()); // время завершения

// id игроков
        int p1 = m.getPlayer1_id();
        int p2 = m.getPlayer2_id();

        UserProfile prof1 = profileRepo.findById(p1).orElseThrow();
        UserProfile prof2 = profileRepo.findById(p2).orElseThrow();

        if (winnerId == null) {

        }
        else if (winnerId.equals(p1)) {
            prof1.setRating(prof1.getRating() + 30);
            prof2.setRating(prof2.getRating() - 30);
        }
        else if (winnerId.equals(p2)) {
            prof2.setRating(prof2.getRating() + 30);
            prof1.setRating(prof1.getRating() - 30);
        }

        profileRepo.save(prof1);
        profileRepo.save(prof2);
        matchRepo.save(m);

        matchRepo.save(m);
    }



}
