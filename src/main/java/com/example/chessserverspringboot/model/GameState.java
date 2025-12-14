package com.example.chessserverspringboot.model;

import java.util.HashMap;
import java.util.Map;

public class GameState {

    private Map<String, String> board = new HashMap<>();

    private boolean whiteToMove = true;
    private int moveNumber = 1;

    public GameState() {
        initBoard();
    }

    /* ================================
            ИНИЦИАЛИЗАЦИЯ ДОСКИ
       ================================ */
    private void initBoard() {

        String[] whitePieces = {"a1:wr", "b1:wn", "c1:wb", "d1:wq", "e1:wk", "f1:wb", "g1:wn", "h1:wr"};
        String[] blackPieces = {"a8:br", "b8:bn", "c8:bb", "d8:bq", "e8:bk", "f8:bb", "g8:bn", "h8:br"};

        for (String p : whitePieces) {
            String[] t = p.split(":");
            board.put(t[0], t[1]);
        }

        for (String p : blackPieces) {
            String[] t = p.split(":");
            board.put(t[0], t[1]);
        }

        // Пешки
        for (char f = 'a'; f <= 'h'; f++) {
            board.put(f + "2", "wp");
            board.put(f + "7", "bp");
        }
    }

    /* ================================
                 СДЕЛАТЬ ХОД
       ================================ */
    public String makeMove(String from, String to) {

        if (!board.containsKey(from))
            return "ERROR";

        String piece = board.get(from);

        boolean isWhitePiece = piece.startsWith("w");

        if (isWhitePiece != whiteToMove)
            return "ERROR";

        // Упрощенная логика — ход разрешён всегда
        board.remove(from);
        board.put(to, piece);

        // Проверка на мат/шах (упрощённо)
        if (isCheckmate())
            return "CHECKMATE";

        if (isCheck())
            return "CHECK";

        // успешный ход
        whiteToMove = !whiteToMove;
        moveNumber++;

        return "OK";
    }

    /* ================================
               ПРОСТАЯ ЛОГИКА ШАХА
       ================================ */
    private boolean isCheck() {
        // Упрощённая версия: шах, если король под атакой (не настоящая логика)
        return false;
    }

    private boolean isCheckmate() {
        return false;
    }

    /* ================================
           ГЕНЕРАЦИЯ FEN ПОЗИЦИИ
       ================================ */
    public String toFEN() {
        StringBuilder fen = new StringBuilder();

        for (int rank = 8; rank >= 1; rank--) {
            int empty = 0;

            for (char file = 'a'; file <= 'h'; file++) {
                String cell = "" + file + rank;

                if (!board.containsKey(cell)) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(pieceToFEN(board.get(cell)));
                }
            }

            if (empty > 0) fen.append(empty);
            if (rank > 1) fen.append('/');
        }

        // Ход
        fen.append(whiteToMove ? " w " : " b ");

        // Рокировки — всегда доступны (упрощённо)
        fen.append("KQkq - 0 ").append(moveNumber);

        return fen.toString();
    }

    private char pieceToFEN(String p) {
        char c = switch (p.substring(1)) {
            case "p" -> 'p';
            case "r" -> 'r';
            case "n" -> 'n';
            case "b" -> 'b';
            case "q" -> 'q';
            case "k" -> 'k';
            default -> '?';
        };
        return p.charAt(0) == 'w' ? Character.toUpperCase(c) : c;
    }

    /* ================================
                   GETTERS
       ================================ */

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public Map<String, String> getBoard() {
        return board;
    }
}
