package src.main.java.Pieces;

import src.main.java.ChessColor;
import src.main.java.Piece;
import src.main.java.Position;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(ImageIcon texture, ChessColor color, Position startPos) {
        super(texture, "Pawn", color, startPos);
    }

    @Override
    public List<Position> getValidPositions() {
        List<Position> validPositions = new ArrayList<>();
        int direction = (this.color == ChessColor.WHITE) ? -1 : 1;

        int currentRow = this.position.x;
        int currentCol = this.position.y;

        // Forward one square
        validPositions.add(new Position(currentRow + direction, currentCol));

        // Forward two squares if at start position
        if (!this.hasMoved) {
            validPositions.add(new Position(currentRow + 2 * direction, currentCol));
        }

        // Diagonal captures (left and right)
        validPositions.add(new Position(currentRow + direction, currentCol - 1));
        validPositions.add(new Position(currentRow + direction, currentCol + 1));


        return validPositions;
    }
}
