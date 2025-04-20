package src.main.java;

import src.main.java.Pieces.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class Board {
    private List<Piece> pieces;

    public Board() {
        this.pieces = new ArrayList<>();
        initializeBoard();
    }

    private void initializeBoard() {
        String[] order = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};

        // mirror board creation
        for (int i = 0; i < 8; i++) {
            pieces.add(createPiece(order[i], ChessColor.BLACK, 0, i));
            pieces.add(createPiece("pawn", ChessColor.BLACK, 1, i));
            pieces.add(createPiece("pawn", ChessColor.WHITE, 6, i));
            pieces.add(createPiece(order[i], ChessColor.WHITE, 7, i));
        }
    }

    private Piece createPiece(String name, ChessColor color, int row, int col) {
        Position pos = new Position(row, col);

        return switch (name.toLowerCase()) {
            case "pawn" -> new Pawn(color, pos);
            case "rook" -> new Rook(color, pos);
            case "knight" -> new Knight(color, pos);
            case "bishop" -> new Bishop(color, pos);
            case "queen" -> new Queen(color, pos);
            case "king" -> new King(color, pos);
            default -> throw new IllegalArgumentException("Unknown piece name: " + name);
        };
    }

    public void move(Position from, Position to) {
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            if (p.getPosition().equals(from)) {
                p.setPosition(to);
                p.setHasMoved(true);
                break;
            }
        }
    }

    public Piece getPieceAt(Position pos) {
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            if (p.getPosition().equals(pos)) {
                return p;
            }
        }
        return null;
    }

    //ritorno la lista dei pezzi creati
    public List<Piece> getPieces() {
        return pieces;
    }
}
