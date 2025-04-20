package src.main.java;

import src.main.java.Pieces.*;

import java.util.ArrayList;
import java.util.List;


public class Board {
    private final List<Piece> pieces;
    private final int width;
    private final int height;

    public Board() {
        this.pieces = new ArrayList<>();
        this.width = 8;
        this.height = 8;
        initializeBoard();
    }

    public Board(List<Piece> pieces) {
        this.pieces = pieces;
        this.width = 8;
        this.height = 8;
    }

    public Board copy() {
        List<Piece> copiedPieces = new ArrayList<>();
        for (Piece piece : this.pieces) {
            copiedPieces.add(piece.copy());
        }
        return new Board(copiedPieces);
    }


    private void initializeBoard() {
        String[] order = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};

        // mirror board creation
        for (int i = 0; i < this.width; i++) {
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
        for (Piece p : pieces) {
            if (p.getPosition().equals(from)) {
                p.setPosition(to);
                p.setHasMoved(true);
                break;
            }
        }
    }

    public Piece getPieceAt(Position pos) {
        for (Piece p : pieces) {
            if (p.getPosition().equals(pos)) {
                return p;
            }
        }
        return null;
    }

    public void addPiece(Piece piece) {
        this.pieces.add(piece);
    }

    public void deletePieceAt(Position pos) {
        pieces.removeIf(p -> p.getPosition().equals(pos));
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
