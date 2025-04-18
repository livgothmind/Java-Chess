package src.main.java.Pieces;

import src.main.java.ChessColor;
import src.main.java.Piece;
import src.main.java.Position;

import javax.swing.*;
import java.util.List;

public class King extends Piece {
    public King(ImageIcon texture, ChessColor color, Position startPos) {
        super(texture, "King", color, startPos);
    }

    @Override
    public List<Position> getValidPositions() {
        return List.of();
    }
}
