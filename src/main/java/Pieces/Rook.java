package src.main.java.Pieces;

import src.main.java.ChessColor;
import src.main.java.Piece;
import src.main.java.Position;

import javax.swing.*;
import java.util.List;

public class Rook extends Piece {
    public Rook(ImageIcon texture, ChessColor color, Position startPos) {
        super(texture, "Rook", color, startPos);
    }

    @Override
    public List<Position> getValidPositions() {
        List<Position> validPositions = new ArrayList<>();
        int x = this.position.x;
        int y = this.position.y;

        // 4 possible directions (horizontal and vertical)
        int[][] directions = {
                {1, 0}, {-1, 0},  // vertical
                {0, 1}, {0, -1}   // horizontal
        };

        // Rook can move up to 7 steps in any direction
        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                int newX = x + dir[0] * i;
                int newY = y + dir[1] * i;
                validPositions.add(new Position(newX, newY));
            }
        }

        return validPositions;
    }

}
