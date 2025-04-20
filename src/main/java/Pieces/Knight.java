package src.main.java.Pieces;

import src.main.java.ChessColor;
import src.main.java.Piece;
import src.main.java.Position;
import java.util.ArrayList;

import javax.swing.*;
import java.util.List;

public class Knight extends Piece {

    public Knight(ChessColor color, Position startPos) {
        super("Knight", color, startPos);
    }

    @Override
    public List<Position> getValidPositions() {
        List<Position> validPositions = new ArrayList<>();
        int x = this.position.x;
        int y = this.position.y;

        // All 8 possible L-shaped moves
        int[][] moves = {
                {+2, +1}, {+2, -1},
                {-2, +1}, {-2, -1},
                {+1, +2}, {+1, -2},
                {-1, +2}, {-1, -2}
        };

        for (int[] move : moves) {
            validPositions.add(new Position(x + move[0], y + move[1]));
        }

        return validPositions;
    }
}
