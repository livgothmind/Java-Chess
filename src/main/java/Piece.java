package src.main.java;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class Piece {
    protected BufferedImage texture;
    protected String name;
    protected boolean hasMoved; // Needed for castle cases
    protected ChessColor color;
    protected Position position;

    public Piece(String name, ChessColor color, Position startPos) {
        this.name = name;
        this.color = color;
        this.position = startPos;
        this.hasMoved = false;

        String path = String.format("assets/%s_%s.png", color.name().toLowerCase(), name.toLowerCase());
        try {
            this.texture = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Error loading texture for " + name + ": " + e.getMessage());
            this.texture = null;
        }
    }

    public abstract List<Position> getValidPositions();

    public abstract Piece copy();

    //getters e setters utili per board (?) non so se bisogna crearne altri
    public Position getPosition() {
        return position;
    }

    public void setPosition(Position newPos) {
        this.position = newPos;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    public BufferedImage getTexture() {
        return texture;
    }

    public ChessColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Piece{" +
                "name='" + name + '\'' +
                ", hasMoved=" + hasMoved +
                ", color=" + color +
                ", position=" + position +
                '}';
    }
}