package src.main.java.Pieces;

import src.main.java.ChessColor;
import src.main.java.Piece;
import src.main.java.Position;
import java.util.ArrayList;

import javax.swing.*;
import java.util.List;

public class Bishop extends Piece {
    public Bishop(ChessColor color, Position startPos) {
        super("Bishop", color, startPos);
    }

    @Override
    public Piece copy() {
        Bishop copy = new Bishop(this.color, new Position(this.position.x, this.position.y));
        copy.setHasMoved(this.hasMoved());
        return copy;
    }

    @Override
    public List<Position> getValidPositions() {
        List<Position> validPositions = new ArrayList<>();
        int x = this.position.x;
        int y = this.position.y;

        for (int i = 1; i < 8; i++) {
            validPositions.add(new Position(x + i, y + i)); // down-right
            validPositions.add(new Position(x + i, y - i)); // down-left
            validPositions.add(new Position(x - i, y + i)); // up-right
            validPositions.add(new Position(x - i, y - i)); // up-left
        }

        return validPositions;
    }

}
