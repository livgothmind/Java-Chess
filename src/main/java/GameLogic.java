package src.main.java;

import src.main.java.Pieces.*;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private int moveNumber;
    private ChessColor turn;
    private final Board board;
    private List<List<Piece>> capturedPieces;

    public GameLogic(int moveNumber, ChessColor turn, Board board, List<List<Piece>> alreadyCapturedPieces) {
        this.moveNumber = moveNumber;
        this.turn = turn;
        this.board = board;
        this.capturedPieces = alreadyCapturedPieces;
    }

    public GameLogic() {
        this.moveNumber = 0;
        this.turn = ChessColor.WHITE;
        this.board = new Board();
        this.capturedPieces = List.of(new ArrayList<>(), new ArrayList<>());
    }

    public ChessColor getTurn() {
        return turn;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isMoveValid(Position from, Position to) {
        // CHECK ALL THE CONDITIONS
        Piece fromPiece = this.board.getPieceAt(from);
        Piece toPiece = this.board.getPieceAt(to);

        // Check if there's no piece at the source position
        if (fromPiece == null) {
            return false;
        }

        // Check if there's a piece at the destination and it's the same color
        if (toPiece != null && fromPiece.getColor() == toPiece.getColor()) {
            return false;
        }

        // Check if 'to' is in the valid positions of 'from'
        if (!fromPiece.getValidPositions().contains(to)) {
            return false;
        }

        // For Rooks and Queens (vertical or horizontal moves)
        if (fromPiece instanceof Rook || fromPiece instanceof Queen) {
            if (from.x == to.x) { // Vertical move (same column)
                int minY = Math.min(from.y, to.y);
                int maxY = Math.max(from.y, to.y);
                for (int y = minY + 1; y < maxY; y++) {
                    if (this.board.getPieceAt(new Position(from.x, y)) != null) {
                        return false; // Piece is blocking the path
                    }
                }
            } else if (from.y == to.y) { // Horizontal move (same row)
                int minX = Math.min(from.x, to.x);
                int maxX = Math.max(from.x, to.x);
                for (int x = minX + 1; x < maxX; x++) {
                    if (this.board.getPieceAt(new Position(x, from.y)) != null) {
                        return false; // Piece is blocking the path
                    }
                }
            }
        }

        // For Bishops and Queens (diagonal moves)
        if (fromPiece instanceof Bishop || fromPiece instanceof Queen) {
            int deltaX = Math.abs(from.x - to.x);
            int deltaY = Math.abs(from.y - to.y);
            if (deltaX == deltaY) { // Ensure the move is diagonal
                int stepX = (to.x > from.x) ? 1 : -1;
                int stepY = (to.y > from.y) ? 1 : -1;
                int x = from.x + stepX;
                int y = from.y + stepY;
                while (x != to.x && y != to.y) {
                    if (this.board.getPieceAt(new Position(x, y)) != null) {
                        return false; // Piece is blocking the path
                    }
                    x += stepX;
                    y += stepY;
                }
            }
        }

        // For Pawns (straight move or capture move)
        if (fromPiece instanceof Pawn) {
            // Regular move (one square forward)
            if (from.y == to.y) { // Only check straight-line moves
                if (to.x == from.x + 1 || to.x == from.x - 1) { // Assuming pawn can move forward by 1 step
                    if (this.board.getPieceAt(to) != null) {
                        return false; // There's a piece blocking the straight move
                    }
                }
            }

            // Pawn capturing move (diagonal)
            if (Math.abs(from.x - to.x) == 1 && Math.abs(from.y - to.y) == 1) {
                if (this.board.getPieceAt(to) == null) {
                    return false; // No opponent piece to capture
                }
            }
        }

        return true;
    }

    public void updateState(Position from, Position to) {
        moveNumber++;
        if (turn == ChessColor.WHITE)
            turn = ChessColor.BLACK;
        else
            turn = ChessColor.WHITE;
        // delete captured piece
        if (this.board.getPieceAt(to) != null) {
            this.board.deletePieceAt(to);
        }
        this.board.move(from, to);
    }

    public boolean move(Position from, Position to) {
        if (this.isMoveValid(from, to)) {
            updateState(from, to);
            return true;
        }
        return false;
    }
}
