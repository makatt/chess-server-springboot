package com.example.chessserverspringboot.model;

public class GameState {

    private Piece[][] board = new Piece[8][8];
    private String currentTurn = "white";

    public GameState() { setupBoard(); }

    private void setupBoard() {
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece("pawn", "black");
            board[6][i] = new Piece("pawn", "white");
        }
        board[0][0] = board[0][7] = new Piece("rook", "black");
        board[7][0] = board[7][7] = new Piece("rook", "white");
        board[0][1] = board[0][6] = new Piece("knight", "black");
        board[7][1] = board[7][6] = new Piece("knight", "white");
        board[0][2] = board[0][5] = new Piece("bishop", "black");
        board[7][2] = board[7][5] = new Piece("bishop", "white");
        board[0][3] = new Piece("queen", "black");
        board[0][4] = new Piece("king", "black");
        board[7][3] = new Piece("queen", "white");
        board[7][4] = new Piece("king", "white");
    }

    public String getCurrentTurn() { return currentTurn; }
    public Piece[][] getBoard() { return board; }

    /** Возвращает результат попытки сделать ход */
    public String makeMove(String from, String to) {
        Position f = Position.fromAlgebraic(from);
        Position t = Position.fromAlgebraic(to);
        Piece piece = board[f.getRow()][f.getCol()];
        if (piece == null || !piece.getColor().equals(currentTurn)) return "ILLEGAL";

        Piece captured = board[t.getRow()][t.getCol()];
        if (captured != null && captured.getColor().equals(piece.getColor())) return "ILLEGAL";

        if (!isValidMove(piece, f, t)) return "ILLEGAL";

        // временно делаем ход
        board[t.getRow()][t.getCol()] = piece;
        board[f.getRow()][f.getCol()] = null;

        // проверяем — не оставил ли игрок своего короля под шахом
        if (isKingInCheck(piece.getColor())) {
            // откатываем
            board[f.getRow()][f.getCol()] = piece;
            board[t.getRow()][t.getCol()] = captured;
            return "SELF_CHECK";
        }

        // меняем очередь
        currentTurn = currentTurn.equals("white") ? "black" : "white";

        // проверяем состояние противника
        String enemy = currentTurn;
        if (isKingInCheck(enemy)) {
            if (!hasLegalMoves(enemy)) return "CHECKMATE";
            return "CHECK";
        }
        return "OK";
    }

    // ---------- Проверка шаха и мата ----------

    private boolean isKingInCheck(String color) {
        Position king = findKing(color);
        if (king == null) return false;

        String enemy = color.equals("white") ? "black" : "white";
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor().equals(enemy)) {
                    if (isValidMove(p, new Position(r, c), king)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Position findKing(String color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (board[r][c] != null &&
                        board[r][c].getType().equals("king") &&
                        board[r][c].getColor().equals(color))
                    return new Position(r, c);
        return null;
    }

    private boolean hasLegalMoves(String color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor().equals(color)) {
                    for (int r2 = 0; r2 < 8; r2++)
                        for (int c2 = 0; c2 < 8; c2++) {
                            Position f = new Position(r, c);
                            Position t = new Position(r2, c2);
                            Piece target = board[r2][c2];
                            if (target != null && target.getColor().equals(color)) continue;
                            if (isValidMove(p, f, t)) {
                                // делаем временный ход
                                board[r2][c2] = p;
                                board[r][c] = null;
                                boolean check = isKingInCheck(color);
                                // откат
                                board[r][c] = p;
                                board[r2][c2] = target;
                                if (!check) return true;
                            }
                        }
                }
            }
        }
        return false;
    }

    // ---------- Проверки типов фигур (из прошлой версии) ----------
    private boolean isValidMove(Piece piece, Position from, Position to) {
        int dr = to.getRow() - from.getRow();
        int dc = to.getCol() - from.getCol();
        switch (piece.getType()) {
            case "pawn":   return validatePawn(piece, from, to, dr, dc);
            case "rook":   return validateRook(from, to, dr, dc);
            case "bishop": return validateBishop(from, to, dr, dc);
            case "knight": return validateKnight(dr, dc);
            case "queen":  return validateQueen(from, to, dr, dc);
            case "king":   return validateKing(dr, dc);
            default:       return false;
        }
    }

    private boolean validatePawn(Piece piece, Position from, Position to, int dr, int dc) {
        int dir = piece.getColor().equals("white") ? -1 : 1;
        Piece target = board[to.getRow()][to.getCol()];

        // Ход вперёд
        if (dc == 0 && target == null) {
            if (dr == dir) return true; // обычный шаг
            if ((piece.getColor().equals("white") && from.getRow() == 6 && dr == -2 && board[5][from.getCol()] == null))
                return true;
            if ((piece.getColor().equals("black") && from.getRow() == 1 && dr == 2 && board[2][from.getCol()] == null))
                return true;
        }

        // Взятие по диагонали
        if (Math.abs(dc) == 1 && dr == dir && target != null && !target.getColor().equals(piece.getColor()))
            return true;

        return false;
    }

    private boolean validateRook(Position from, Position to, int dr, int dc) {
        if (dr != 0 && dc != 0) return false;
        return pathClear(from, to);
    }

    private boolean validateBishop(Position from, Position to, int dr, int dc) {
        if (Math.abs(dr) != Math.abs(dc)) return false;
        return pathClear(from, to);
    }

    private boolean validateKnight(int dr, int dc) {
        return (Math.abs(dr) == 2 && Math.abs(dc) == 1) ||
                (Math.abs(dr) == 1 && Math.abs(dc) == 2);
    }

    private boolean validateQueen(Position from, Position to, int dr, int dc) {
        return validateRook(from, to, dr, dc) || validateBishop(from, to, dr, dc);
    }

    private boolean validateKing(int dr, int dc) {
        return Math.abs(dr) <= 1 && Math.abs(dc) <= 1;
    }

    // Проверка, свободен ли путь для ладьи/слона/ферзя
    private boolean pathClear(Position from, Position to) {
        int dr = Integer.compare(to.getRow(), from.getRow());
        int dc = Integer.compare(to.getCol(), from.getCol());
        int r = from.getRow() + dr, c = from.getCol() + dc;

        while (r != to.getRow() || c != to.getCol()) {
            if (board[r][c] != null) return false;
            r += dr; c += dc;
        }
        return true;
    }
}
