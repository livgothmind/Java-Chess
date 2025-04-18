package src.main.java;

import javax.swing.*;
import src.main.java.Position;

import java.util.List;

public abstract class Piece {
    protected ImageIcon texture;
    protected String name;
    protected boolean hasMoved; // Needed for castle cases
    protected ChessColor color;
    protected Position position;

    public Piece(ImageIcon texture, String name, ChessColor color, Position startPos) {
        this.texture = texture;
        this.name = name;
        this.color = color;
        this.position = startPos;

        this.hasMoved = false;
    }


    public abstract List<Position> getValidPositions();
}
