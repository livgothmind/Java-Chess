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

        for (int i = 0; i < 8; i++) {
            pieces.add(createPiece(order[i], ChessColor.BLACK, 0, i));
            pieces.add(createPiece("pawn", ChessColor.BLACK, 1, i));
            pieces.add(createPiece("pawn", ChessColor.WHITE, 6, i));
            pieces.add(createPiece(order[i], ChessColor.WHITE, 7, i));
        }
    }

    private Piece createPiece(String name, ChessColor color, int row, int col) {
        String path = String.format("assets/%s_%s.png", color.name().toLowerCase(), name.toLowerCase());
        ImageIcon icon = new ImageIcon(path);
        Position pos = new Position(row, col);

        return switch (name.toLowerCase()) {
            case "pawn" -> new Pawn(icon, color, pos);
            case "rook" -> new Rook(icon, color, pos);
            case "knight" -> new Knight(icon, color, pos);
            case "bishop" -> new Bishop(icon, color, pos);
            case "queen" -> new Queen(icon, color, pos);
            case "king" -> new King(icon, color, pos);
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
