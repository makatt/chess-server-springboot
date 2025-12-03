package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.Entity.Match;
import com.example.chessserverspringboot.Entity.MatchMove;
import com.example.chessserverspringboot.Repository.MatchRepository;
import com.example.chessserverspringboot.Repository.MatchMovesRepository;
import com.example.chessserverspringboot.model.MoveJson;
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
            // разбиваем "e2-e4"
            String[] parts = moveData.split("-");

            // JSON-структура хода
            ObjectNode json = mapper.createObjectNode();
            json.put("moveNumber", number);
            json.put("playerId", playerId);
            json.put("from", parts[0]);
            json.put("to", parts[1]);
            json.put("raw", moveData);
            json.put("timestamp", LocalDateTime.now().toString());


            MatchMove mv = new MatchMove();
            mv.setMatch_id(matchId);
            mv.setPlayer_id(playerId);
            mv.setMove_number(number);
            String jsonStr = mapper.writeValueAsString(json);
            mv.setMove_data(jsonStr);

            mv.setFen_position(fen);

            moveRepo.save(mv);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void finishMatch(int matchId, int winnerId, String fen, String reason) {
        Match m = matchRepo.findById(matchId).orElseThrow();
        m.setWinner_id(winnerId);
        m.setEnd_reason(reason);
        m.setFinal_fen(fen);
        m.setEnded_at(LocalDateTime.now());
        matchRepo.save(m);
    }
}
