package src.main.java.Pieces;

import src.main.java.ChessColor;
import src.main.java.Piece;
import src.main.java.Position;
import java.util.ArrayList;

import javax.swing.*;
import java.util.List;

public class King extends Piece {
    public King(ImageIcon texture, ChessColor color, Position startPos) {
        super(texture, "King", color, startPos);
    }

    @Override
    public List<Position> getValidPositions() {
        List<Position> validPositions = new ArrayList<>();
        int x = this.position.x;
        int y = this.position.y;

        // Standard king moves
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                validPositions.add(new Position(x + dx, y + dy));
            }
        }

        // Add castling moves (we only suggest them, no validation here)
        if (!this.hasMoved) {
            int row = (this.color == ChessColor.WHITE) ? 7 : 0;
            if (x == row && y == 4) { // default king position
                // Kingside castling (to g1/g8)
                validPositions.add(new Position(row, 6));

                // Queenside castling (to c1/c8)
                validPositions.add(new Position(row, 2));
            }
        }

        return validPositions;
    }

}
